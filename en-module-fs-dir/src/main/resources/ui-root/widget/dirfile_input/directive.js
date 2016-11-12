var C2SingleUploadDirFile = function (scope, element, attrs) {
    this.$dom = element;
    this.$scope = scope;
    this.attrs = attrs;
};

C2SingleUploadDirFile.prototype = {
    upload: function (scEvent,erEvent){
        this.$scope.upload(scEvent,erEvent);
    },
    getSize:function(){
    	var f=$("#"+this.attrs.id).val();
		if(undefined!=f||""!=f){
			return 1;
		}
		return 0;
    },
    setDirPath:function(path){
    	if(angular.isUndefined(path)||""==path) return;
    	var formObj=$("#upload_form_"+this.attrs.id);
    	var dirPathInput=formObj.find("#dirPath");
    	dirPathInput.val(path);
    }
};

directives.directive('c2SingleUploadDirFile', ['$rootScope','FormContainerFactory','Messenger','$timeout',
                                      function ($rootScope,FormContainerFactory,Messenger,$timeout) {
 return {
 	scope:{
 		onSuccess:'&',
 		onError:'&'
 	},
    controller: function($scope,$attrs){
    	
    	var directiveId=$attrs.id;
    	$scope.showRemoveButton = angular.isDefined($attrs.showRemoveButton);
    	
    	$scope.uploading=false;
    	
    	$scope.successCallback=function(data){
    		
    		$scope.uploading=false;
    		
    		$("#formGroup-"+directiveId).find(".message-loading-overlay").remove();
    		
    		if($scope.successEvent){
    			$scope.successEvent(data.data);
    		}
    		
    		if(!angular.isUndefined($scope.onSuccess)){
    			$scope.onSuccess(data);
    		}
    	}
    	
    	$scope.errorCallback=function(data){
    		
    		$scope.uploading=false;
    		
    		$("#formGroup-"+directiveId).find(".message-loading-overlay").remove();
    		
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
			if($("#"+directiveId).val()==undefined||$("#"+directiveId).val()==""){
				Messenger.post({type:'error',message:'附件不能为空！'});
				return;
			}
			
			//js动态注册事件
    		if(scEvent!=undefined&&$.isFunction(scEvent)){
    			$scope.successEvent=scEvent;
    		}
    		if(erEvent!=undefined&&$.isFunction(erEvent)){
    			$scope.errorEvent=erEvent;
    		}
    		
    		$scope.uploading=true;
    		
    		$("#formGroup-"+directiveId).append("<div  style=\"filter: alpha(opacity=50); opacity: 0.5; background-color: #000;\" class=\"message-loading-overlay\"><i class=\"fa-spin ace-icon fa fa-spinner orange2 bigger-160\"></i></div>");
    		
    		$scope.formObj.submit();
    		
    	}
	},
 	compile:function($element,$attrs){
 		return function($scope,$element,$attrs){
 			var submitUrl="iframefile/"+$attrs.process+"/upload";
 			var iframeObj;
 			var formObj;
 			
 			var form=FormContainerFactory.findForm($scope);
            form[$attrs.id] = new C2SingleUploadDirFile($scope, $element,$attrs);
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
    		if($("#"+iframeId).length>0){
    			iframeObj=$("#"+iframeId);
    		}else{
    			iframeObj=$("<iframe name=\""+iframeId+"\" id=\""+iframeId+"\" style=\"display:none\"></iframe>");
    			$element.after(iframeObj);
    		}
    		
    		if($("#"+formId).length>0){
    			formObj=$("#"+formId);
    			formObj.empty();
    		}else{
    			formObj=$("<form action=\""+submitUrl+"\" id=\""+formId+"\" name=\""+formId+"\" encType=\"multipart/form-data\"  method=\"post\" target=\""+iframeId+"\">" +
    					"</form>");
    			$element.after(formObj);
    			formObj.append($element);
    		}
    		
			formObj.append("<input type=\"hidden\" name=\"controlId\" value=\""+$attrs.id+"\">");
			
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