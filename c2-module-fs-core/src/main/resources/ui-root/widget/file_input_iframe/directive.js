var C2SingleUploadFileIframe = function (scope, element, attrs) {
    this.$dom = element;
    this.$scope = scope;
    this.attrs = attrs;
    this.params=undefined;
};

C2SingleUploadFileIframe.prototype = {
	getDom:function(){
		return this.$dom;
	},
    upload: function (scEvent,erEvent){
        this.$scope.upload(scEvent,erEvent);
    },
    getValue:function(){
    	return this.$scope.formObj.find("input[c2-single-upload-file-iframe]").val();
    },
    setProcess:function(process){
    	var submitUrl="iframefile/"+process+"/upload";
    	this.$scope.formObj.attr("action",submitUrl);
    },
    setTargetUrl:function(url){
    	this.$scope.formObj.attr("action",url);
    },
    setParams:function(params){
    	this.params=params;
    },
    reset:function(){
    	this.$scope.formObj.find("input[c2-single-upload-file-iframe]").val("");
    	this.$dom.ace_file_input('reset_input_ui');
    },
    submit:function(scEvent,erEvent){
    	this.$scope.upload(scEvent,erEvent);
    }
};

directives.directive('c2SingleUploadFileIframe', ['$rootScope','FormContainerFactory','Messenger','$timeout',
                                      function ($rootScope,FormContainerFactory,Messenger,$timeout) {
 return {
 	scope:{
 		onSuccess:'&',
 		onError:'&',
		allow:'@',
		deny:'@'
 	},
    controller: function($scope,$attrs){
    	
    	$scope.instanceId=$attrs.id+"_"+new Date().getTime()+"_"+Math.floor((Math.random()*10000000000));

    	$scope.showRemoveButton = angular.isDefined($attrs.showRemoveButton);
    	
    	$scope.uploading=false;
    	
    	$scope.successCallback=function(data){

    		$scope.uploading=false;
    		
    		$scope.widgetContainer.find(".message-loading-overlay").remove();
    		
    		if($scope.successEvent){
    			$scope.successEvent(data.data);
    		}
    		
    		if(!angular.isUndefined($scope.onSuccess)){
    			$scope.onSuccess(data);
    		}
    	}
    	
    	$scope.errorCallback=function(data){

    		$scope.uploading=false;
    		
    		$scope.widgetContainer.find(".message-loading-overlay").remove();
    		
    		if($scope.errorEvent){
    			$scope.errorEvent(data.data);
    		}
    		
    		if(!angular.isUndefined($scope.onError)){
    			$scope.onError(data);
    		}
    	}
    	
    	$scope.upload=function(scEvent,erEvent){
    		
    		var fileDom=$scope.widgetContainer.find("input[type='file']");
    		
    		//提交前验证
    		if(fileDom.length>1){
    			Messenger.post({type:'error',message:"【"+$attrs.id+"】控件ID冲突，请检查表单控件ID唯一性！"});
    			return;
    		}
    		
    		if($scope.uploading){
    			Messenger.post({type:'error',message:'正在上传,请耐心等待!'});
    			return;
    		}
    		
			if(fileDom.val()==undefined||fileDom.val()==""){
				Messenger.post({type:'error',message:"附件不能为空！"});
				return;
			}

			//准备动态参数
			if($scope.c2SingleUploadFileIframe.params instanceof Object){
				for (name in $scope.c2SingleUploadFileIframe.params) {
					if($scope.c2SingleUploadFileIframe.params.hasOwnProperty(name)){
						var f=$scope.formObj.find("#"+name);
						if(f.length>0){
							f.val($scope.c2SingleUploadFileIframe.params[name]);	
						}else{
							$scope.formObj.append("<input type=\"hidden\" id=\""+name+"\" name=\""+name+"\" value=\""+$scope.c2SingleUploadFileIframe.params[name]+"\">");
						}
					}
				}
			}
			
			//js动态注册事件
    		if(scEvent!=undefined&&$.isFunction(scEvent)){
    			$scope.successEvent=scEvent;
    		}
    		if(erEvent!=undefined&&$.isFunction(erEvent)){
    			$scope.errorEvent=erEvent;
    		}
    		
    		$scope.uploading=true;
    		
    		$scope.widgetContainer.append("<div style=\"filter: alpha(opacity=50); opacity: 0.5; background-color: #000;\" class=\"message-loading-overlay\"><i class=\"fa-spin ace-icon fa fa-spinner orange2 bigger-160\"></i></div>");
    		$scope.formObj.submit();
    	}
	},
	template:"<span id=\"content\"></span>",
 	compile:function($element,$attrs){
 		return function($scope,$element,$attrs){
 			
 			//为控件打上实例标记
 			$element.attr("data-instanceid",$scope.instanceId);
 			
 			var submitUrl="iframefile/"+$attrs.process+"/upload";
 			var iframeObj;
 			var formObj;
 			
 			
 			var form=FormContainerFactory.findForm($scope);
 			
 			//如果同一个表单中存在多个，[$view.控件id]变为数组类型
 			var widgetHandle=form[$attrs.id];
 			$scope.c2SingleUploadFileIframe=new C2SingleUploadFileIframe($scope, $element,$attrs);
 			
 			if(widgetHandle){
 				if($.isArray(widgetHandle)){
 					widgetHandle.push($scope.c2SingleUploadFileIframe);
 				}else{
 					var arr=new Array();
 					arr.push(widgetHandle);
 					arr.push($scope.c2SingleUploadFileIframe);
 					form[$attrs.id]=arr;
 				}
 			}else{
 				form[$attrs.id] =$scope.c2SingleUploadFileIframe;
 			}

            $scope.autoSubmit=$attrs.autoSubmit;
            
 			var opts={
                    no_file: '未选中文件 ...',
                    btn_choose: '浏览...',
                    btn_change: '修改...',
                    droppable: false,
                    thumbnail: false,
                    icon_remove: $scope.showRemoveButton?'fa fa-times':false
                };
        	if(angular.isDefined($scope.allow)){
        		opts.allowExt=angular.fromJson($scope.allow);
        	}
        	if(angular.isDefined($scope.deny)){
        		opts.denyExt=angular.fromJson($scope.deny);
        	}
        	
 			var iframeId="upload_iframe_"+$scope.instanceId;
 			var formId="upload_form_"+$scope.instanceId;
    		if($element.find("#content #"+iframeId).length>0){
    			iframeObj=$element.find("#content #"+iframeId);
    		}else{
    			iframeObj=$element.find("#content").append("<iframe name=\""+iframeId+"\" id=\""+iframeId+"\" style=\"display:none\"></iframe>");
    		}
    		
    		
    		if($element.find("#content #"+formId).length>0){
    			formObj=$element.find("#content #"+formId);
    			formObj.empty();
    		}else{
    			formObj=$("<form action=\""+submitUrl+"\" id=\""+formId+"\" name=\""+formId+"\" encType=\"multipart/form-data\"  method=\"post\" target=\""+iframeId+"\">" +
    					"</form>");
    			$element.find("#content").append(formObj);
    		}
    		
    		var fileElement=$("<input name=\"file_"+$scope.instanceId+"+\" id=\"file_"+$scope.instanceId+"+\" type=\"file\"/>");
    		formObj.append(fileElement);

    		fileElement.ace_file_input(opts).on('file.error.ace',function(){
        		Messenger.error("不支持上传该类型的文件，只支持: "+$scope.allow);
        	});
    		
			formObj.append("<input type=\"hidden\" name=\"controlId\" value=\""+$attrs.id+"\">");
			formObj.append("<input type=\"hidden\" name=\"instanceId\" value=\""+$scope.instanceId+"\">");
			
			if(!angular.isUndefined($attrs.maxSize)){
				formObj.append("<input type=\"hidden\" id=\"maxSize\" name=\"maxSize\" value=\""+$attrs.maxSize+"\">");
			}
    		$scope.iframeObj=iframeObj;
    		$scope.formObj=formObj;
    		
    		$scope.widgetContainer=$element;
    		
    		fileElement.bind('change', function(evt) {
        		
        		//提交前验证
    			if($(this).val()==undefined||$(this).val()==""){
    				$scope.formObj.parent().find(".remove").click();
    				return;
    			}
       			
    			//html5验证大小
    			if(this.files&&this.files.length>0){
    				$attrs.maxSize=parseInt($attrs.maxSize);
    				if(!angular.isNumber($attrs.maxSize)){
    					$attrs.maxSize=undefined;
    				}
    				
    				if($attrs.maxSize&&this.files[0].size>$attrs.maxSize){
    					var sizeLabel=$attrs.maxSize+"字节";
    					if($attrs.maxSize>=1024&&$attrs.maxSize<(1024*1024)){
    						var sizeLabel=($attrs.maxSize/1024)+"KB";
    					}else if($attrs.maxSize>=(1024*1024)){
    						var sizeLabel=($attrs.maxSize/(1024*1024))+"MB";
    					}
    					Messenger.error("文件大小不能超过"+sizeLabel);
    					$scope.formObj.parent().find(".remove").click();
    					return;
    				}
    			}
    			
    			if($scope.autoSubmit!=undefined){
        			$timeout(function() {
        				$scope.upload();
        			});
    			}
    		});
    		if (('ontouchstart' in window) ||
    				(navigator.maxTouchPoints > 0) || (navigator.msMaxTouchPoints > 0)) {
    				fileElement.bind('touchend', function(e) {
        				e.preventDefault();
        				e.target.click();
        			});
    			}
            }
 		}
 	}
 }]);