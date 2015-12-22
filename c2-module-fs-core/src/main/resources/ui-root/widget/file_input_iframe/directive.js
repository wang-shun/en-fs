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
    	
    	var directiveId=$attrs.id;
    	$scope.showRemoveButton = angular.isDefined($attrs.showRemoveButton);
    	
    	$scope.uploading=false;
    	
    	$scope.successCallback=function(data){
    		
    		var form=FormContainerFactory.findForm($scope);
    		var groupDom=form.mainForm.getDom().find("#formGroup-"+directiveId);
    		
    		$scope.uploading=false;
    		
    		groupDom.find(".message-loading-overlay").remove();
    		
    		if($scope.successEvent){
    			$scope.successEvent(data.data);
    		}
    		
    		if(!angular.isUndefined($scope.onSuccess)){
    			$scope.onSuccess(data);
    		}
    	}
    	
    	$scope.errorCallback=function(data){
    		
    		var form=FormContainerFactory.findForm($scope);
    		var groupDom=form.mainForm.getDom().find("#formGroup-"+directiveId);
    		
    		$scope.uploading=false;
    		
    		groupDom.find(".message-loading-overlay").remove();
    		
    		if($scope.errorEvent){
    			$scope.errorEvent(data.data);
    		}
    		
    		if(!angular.isUndefined($scope.onError)){
    			$scope.onError(data);
    		}
    	}
    	
    	$scope.upload=function(scEvent,erEvent){
    		var form=FormContainerFactory.findForm($scope);
    		var fileDom=form.mainForm.getDom().find("input[c2-single-upload-file-iframe][name="+directiveId+"]");
    		
    		//提交前验证
    		if(fileDom.length>1){
    			Messenger.post({type:'error',message:"【"+directiveId+"】控件ID冲突，请检查表单控件ID唯一性！"});
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
	    	var c2SingleUploadFileIframe=form[directiveId];
			if(c2SingleUploadFileIframe.params instanceof Object){
				for (name in c2SingleUploadFileIframe.params) {
					if(c2SingleUploadFileIframe.params.hasOwnProperty(name)){
						var f=$scope.formObj.find("#"+name);
						if(f.length>0){
							f.val(c2SingleUploadFileIframe.params[name]);	
						}else{
							$scope.formObj.append("<input type=\"hidden\" id=\""+name+"\" name=\""+name+"\" value=\""+c2SingleUploadFileIframe.params[name]+"\">");
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
    		
    		var groupDom=form.mainForm.getDom().find("#formGroup-"+directiveId);
    		groupDom.append("<div style=\"filter: alpha(opacity=50); opacity: 0.5; background-color: #000;\" class=\"message-loading-overlay\"><i class=\"fa-spin ace-icon fa fa-spinner orange2 bigger-160\"></i></div>");
    		$scope.formObj.submit();
    	}
	},
 	compile:function($element,$attrs){
 		return function($scope,$element,$attrs){
 			
 			var instanceId=$attrs.id+"_"+Math.random();
 			var submitUrl="iframefile/"+$attrs.process+"/upload";
 			var iframeObj;
 			var formObj;
 			
 			var form=FormContainerFactory.findForm($scope);
            form[$attrs.id] = new C2SingleUploadFileIframe($scope, $element,$attrs);
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
        	$element.ace_file_input(opts).on('file.error.ace',function(){
        		Messenger.error("不支持上传该类型的文件，只支持: "+$scope.allow);
        	});
        	
 			var iframeId="upload_iframe_"+$attrs.id;
 			var formId="upload_form_"+$attrs.id;
    		if($element.parent().find("#"+iframeId).length>0){
    			iframeObj=$element.parent().find("#"+iframeId);
    		}else{
    			iframeObj=$("<iframe name=\""+iframeId+"\" id=\""+iframeId+"\" style=\"display:none\"></iframe>");
    			$element.after(iframeObj);
    		}
    		
    		if($element.parent().find("#"+formId).length>0){
    			formObj=$element.parent().find("#"+formId);
    			formObj.empty();
    		}else{
    			formObj=$("<form action=\""+submitUrl+"\" id=\""+formId+"\" name=\""+formId+"\" encType=\"multipart/form-data\"  method=\"post\" target=\""+iframeId+"\">" +
    					"</form>");
    			$element.after(formObj);
    			formObj.append($element);
    		}
    		
			formObj.append("<input type=\"hidden\" name=\"controlId\" value=\""+$attrs.id+"\">");
			formObj.append("<input type=\"hidden\" name=\"instanceId\" value=\""+instanceId+"\">");
			
			if(!angular.isUndefined($attrs.maxSize)){
				formObj.append("<input type=\"hidden\" id=\"maxSize\" name=\"maxSize\" value=\""+$attrs.maxSize+"\">");
			}
    		$scope.iframeObj=iframeObj;
    		$scope.formObj=formObj;
			
        	$element.bind('change', function(evt) {
        		
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
    				$element.bind('touchend', function(e) {
        				e.preventDefault();
        				e.target.click();
        			});
    			}
            }
 		}
 	}
 }]);