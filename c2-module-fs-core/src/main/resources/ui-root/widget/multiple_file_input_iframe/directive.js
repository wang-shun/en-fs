var C2MultipleUploadFileIframe = function (scope, element, attrs) {
    this.$dom = element;
    this.$scope = scope;
    this.attrs = attrs;
    this.params=undefined;
};
C2MultipleUploadFileIframe.prototype = {
	submit: function (params,scEvent,erEvent){
		this.params=params;
        this.$scope.upload(scEvent,erEvent);
    },
    getValue:function(){
    	return this.$scope.fileList;
    },
    disable:function(flag){
    	this.$scope.disable(flag);
    },
    setTargetUrl:function(url){
    	var directiveId=this.attrs.id;
    	$("#upload_form_"+directiveId).attr("action",url);
    },
    setParams:function(params){
    	this.params=params;
    },
    reset:function(){
    	this.$scope.fileList=[];
    }
};

directives.directive('c2MultipleUploadFileIframe', ['$rootScope','FormContainerFactory','Messenger','$timeout',
                                      function ($rootScope,FormContainerFactory,Messenger,$timeout) {
 return {
 	scope:{
 		onSuccess:'&',
 		onError:'&'
 	},
    controller: function($scope,$attrs){
    	$scope.instanceId=$attrs.id+"_"+new Date().getTime()+"_"+Math.floor((Math.random()*10000000000));
    	var directiveId=$attrs.id;
    	$scope.title=$attrs.title;
    	$scope.uploading=false;
    	$scope.fileList=[];
    	
    	$scope.disabled=($attrs.disabled=="true");
    	
    	$scope.disable=function(flag){
    		//#attr("ng-disabled='${param.disabled}'" $!{param.disabled})
    		if(true==flag){
    			$scope.disabled=true;
    			$scope.widgetContainer.find(".file-input").css({display:"none"});
    			$scope.widgetContainer.find(".glyphicon-remove").removeClass("red");
    		}else{
    			$scope.widgetContainer.find(".file-input").css({display:"block"});
    			$scope.widgetContainer.find(".glyphicon-remove").addClass("red");
    			$scope.disabled=false;
    		}
    	}
    	
    	$scope.addFile=function(){
    		
    		//创建临时文件选择控件
    		var fileObjId=$scope.instanceId+"_"+new Date().getTime();
    		var fileObj;
    		fileObj=$("<input id=\""+fileObjId+"\" name=\""+fileObjId+"\"class=\"file-input\" type=\"file\"></input>");
    		fileObj.on("change",function(evt) {

    				//添加时验证
    				if($.isNumeric($attrs.maxNum)){
    					var fNum=$scope.formObj.find(".file-input").length;
    					if(parseInt($attrs.maxNum)>0&&fNum+1>parseInt($attrs.maxNum)){
  					      Messenger.post({type:'error',message:'您最多添加【'+$attrs.maxNum+'】个附件！'});
  					      return;
    					}
    				}
    			
        			var fileName=evt.target.value;
        			if(""==fileName){
        				return;
        			}
        			fileName=fileName.match(/[^\\]*$/)[0];
        			
        			//验证文件后缀
        			if(angular.isDefined($attrs.allow)||angular.isDefined($attrs.deny)){
        				var extStart = fileName.lastIndexOf(".");
                        var ext = fileName.substring(extStart+1,fileName.length).toUpperCase();
                        if(extStart==-1){
                        	if(angular.isDefined($attrs.allow)){
                        		Messenger.error("不支持上传该类型的文件，只支持: "+$attrs.allow);
                        		return;
                        	}
                        }
                        
                        var isInDeny=false;
                        if(angular.isDefined($attrs.deny)){
                        	var deny=angular.fromJson($attrs.deny);
                        	for (name in deny){
                            	if(deny[name].toUpperCase()==ext){
                            		isInDeny=true;
                            		break;
                            	}
                            }
                        	
                        	if(isInDeny){
                        		Messenger.error("不支持上传该类型的文件: "+$attrs.deny);
                        		return;
                        	}
                        }
                        
                        var isInAllow=false;
                        if(angular.isDefined($attrs.allow)){
                        	var allow=angular.fromJson($attrs.allow);
                        	for (name in allow){
                            	if(allow[name].toUpperCase()==ext){
                            		isInAllow=true;
                            		break;
                            	}
                            }
                        	
                        	if(!isInAllow){
                        		Messenger.error("不支持上传该类型的文件，只支持: "+$attrs.allow);
                        		return;
                        	}
                        }
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
        					Messenger.error("单个文件大小不能超过"+sizeLabel);
        					return;
        				}
        			}
        			
        			var fdata={
        					domId:fileObjId,
        					name:fileName,
        					isDown:false,
        					isDelete:false
        			}
        			
        			
        			if(!angular.isUndefined($attrs.isDown)&&$attrs.isDown=="true"){
        				fdata.isDown=true;
        			}
        			
        			if(!angular.isUndefined($attrs.isDelete)&&$attrs.isDelete=="true"){
        				fdata.isDelete=true;
        			}
        			$scope.fileList.push(fdata);
        			
        			//将input移动form下
        			$("#"+fileObjId).attr("style","display:none");
        			$scope.formObj.append($("#"+fileObjId));
        			
    			    if ($scope.$$phase != '$apply' &&  $scope.$$phase != '$digest') {
    				   $scope.$apply();
    			    }
    			    
    			    $scope.addFile();
        			
        		});
    		
    		//初始化input
    		var fbtn=$scope.widgetContainer.find(".fileinput-button");
    		if(fbtn.length<=0){
    			throw new Error("文件上传模板不正确");
    		}
    		
    		fbtn.append(fileObj);
    		//$scope.formObj.append(fileObj);
    	}
    	
    	$scope.deleteFile=function(domId){
    		
    		if($scope.disabled) return;
    		
    		$("#"+domId).remove();
    		for (var i=0;i<$scope.fileList.length;i++){
    			if($scope.fileList[i].domId==domId){
    				$scope.fileList.splice(i,1);
    			}
    		}
    	}
    	
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
    		
    		if($scope.uploading){
    			Messenger.post({type:'error',message:'正在上传,请耐心等待!'});
    			return;
    		}
    		
    		//提交前验证
			if($.isNumeric($attrs.minNum)){
				var fNum=$scope.formObj.find(".file-input").length;
				if(parseInt($attrs.minNum)>0&&fNum<parseInt($attrs.minNum)){
				      Messenger.post({type:'error',message:'您至少添加【'+$attrs.minNum+'】个附件！'});
				      return;
				}
			}
			
	    	
			if($scope.c2MultipleUploadFileIframe.params instanceof Object){
				for (name in $scope.c2MultipleUploadFileIframe.params) {
					if($scope.c2MultipleUploadFileIframe.params.hasOwnProperty(name)){
						var f=$scope.formObj.find("#"+name);
						if(f.length>0){
							f.val($scope.c2MultipleUploadFileIframe.params[name]);	
						}else{
							$scope.formObj.append("<input type=\"hidden\" id=\""+name+"\" name=\""+name+"\" value=\""+$scope.c2MultipleUploadFileIframe.params[name]+"\">");
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
    		
    		$scope.widgetContainer.append("<div  style=\"filter: alpha(opacity=50); opacity: 0.5; background-color: #000;\" class=\"message-loading-overlay\"><i class=\"fa-spin ace-icon fa fa-spinner orange2 bigger-160\"></i></div>");
    		
    		$scope.uploading=true;
    		$scope.formObj.submit();
    	}
	},
	templateUrl:'widget_multiple_file_input_iframe',
 	compile:function($element,$attrs){
	 		return function($scope,$element,$attrs){
	 			
	 			//为控件打上实例标记
	 			$element.attr("data-instanceid",$scope.instanceId);
	 			
	 			var submitUrl="iframefile/"+$attrs.process+"/upload";
	 			var iframeObj;
	 			var formObj;
	 			
	 			var form=FormContainerFactory.findForm($scope);
	 			
	 			//如果存在多个，[$view.控件id]变为数组类型
	 			var widgetHandle=form[$attrs.id];
	 			$scope.c2MultipleUploadFileIframe=new C2MultipleUploadFileIframe($scope,$element,$attrs)
	 			if(widgetHandle){
	 				if($.isArray(widgetHandle)){
	 					widgetHandle.push($scope.c2MultipleUploadFileIframe);
	 				}else{
	 					var arr=new Array();
	 					arr.push(widgetHandle);
	 					arr.push($scope.c2MultipleUploadFileIframe);
	 					form[$attrs.id]=arr;
	 				}
	 			}else{
	 				form[$attrs.id] = $scope.c2MultipleUploadFileIframe;
	 			}
	 			
	 			
	 			
	    		
	 			var iframeId="upload_iframe_"+$scope.instanceId;
	 			var formId="upload_form_"+$scope.instanceId;
	    		if($element.parent().find("#"+iframeId).length>0){
	    			iframeObj=$element.parent().find("#"+iframeId);
	    		}else{
	    			iframeObj=$("<iframe name=\""+iframeId+"\" id=\""+iframeId+"\" style=\"display:none\"></iframe>");
	    			$element.append(iframeObj);
	    		}
	    		
	    		if($element.parent().find("#"+formId).length>0){
	    			formObj=$element.parent().find("#"+formId);
	    			formObj.empty();
	    		}else{
	    			formObj=$("<form action=\""+submitUrl+"\" id=\""+formId+"\" name=\""+formId+"\" encType=\"multipart/form-data\"  method=\"post\" target=\""+iframeId+"\">" +
	    					"</form>");
	    			$element.append(formObj);
	    		}
	    		
				formObj.append("<input type=\"hidden\" name=\"controlId\" value=\""+$attrs.id+"\">");
				formObj.append("<input type=\"hidden\" name=\"instanceId\" value=\""+$scope.instanceId+"\">");
				
				if(!angular.isUndefined($attrs.maxSize)){
					formObj.append("<input type=\"hidden\" name=\"maxSize\" value=\""+$attrs.maxSize+"\">");
				}
	    		$scope.iframeObj=iframeObj;
	    		$scope.formObj=formObj;
	    		
	    		$scope.widgetContainer=FormContainerFactory.findForm($scope).mainForm.getDom().find("[data-instanceid='"+$scope.instanceId+"']");
	    		
	    		$scope.addFile();
	    		
	    		if("true"==$attrs.disabled){
	    			$scope.disable(true);
	    		}
	 		}
 		}
 	}
 }]);