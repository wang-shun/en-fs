directives.directive('c2FileInputChunk', ['$rootScope','FormContainerFactory','Messenger','$timeout','$ocLazyLoad',
                                     function ($rootScope,FormContainerFactory,Messenger,$timeout,$ocLazyLoad) {
    return {
    	scope:{
    		onSuccess:'&',
    		onError:'&',
    		targetUrl:'@',
    		allow:'@',
    		deny:'@'
    	},
    	controller:function($scope,$element,$attrs){
    		
    		var form = FormContainerFactory.findForm($scope);
    		form[$attrs.id] = this;

    		this.setTargetUrl = function(_url){$scope.targetUrl = _url;};
    		$scope.showRemoveButton = angular.isDefined($attrs.showRemoveButton);
    		$scope.autoSubmit = angular.isDefined($attrs.autoSubmit);
    		
    		var fileUpload=undefined;
    		var messenger=undefined;
    		
    		this.getChunkUpLoader=function(){
    			return $scope.chunkUpLoader;
    		}
    		
    		this.submit = function(){
    			var stats=$scope.chunkUpLoader.getStats();
    			if(stats.progressNum>0){
    				return false;
    			}else if(stats.uploadFailNum>0){
    				$scope.chunkUpLoader.retry();
    			}else{
    				$scope.chunkUpLoader.upload();
    			}
    			
    			this.showProgress();
    			this.showLoadingDiv();
    			
    			return true;
    		}
    		
    		this.hideLoadingDiv=function(){
    			var groupDom=form.mainForm.getDom().find("#formGroup-"+$attrs.id);
    			groupDom.find(".message-loading-overlay").remove();
    		}
    		
    		this.showLoadingDiv=function(){
        		var groupDom=form.mainForm.getDom().find("#formGroup-"+$attrs.id);
        		groupDom.append("<div style=\"filter: alpha(opacity=50); opacity: 0.5; background-color: #000;\" class=\"message-loading-overlay\"><i class=\"fa-spin ace-icon fa fa-spinner orange2 bigger-160\"></i></div>");
    		}

    		this.updateProgress=function(percentage){
    			if(undefined==messenger) return;
    			$(messenger.el).find("#upload_msg_"+$attrs.id).text("文件上传中，进度:"+parseInt(percentage * 100) +"%");
    		}
    		
    		this.setCookie=function(name,value){
    			var exdate=new Date();
    			exdate.setDate(exdate.getDate() + 7);
    			document.cookie=name+ "=" + escape(value)+";expires="+exdate.toGMTString();
    		}
    		
    		this.getCookie=function(key){
    		　　　　if (document.cookie.length>0){
    		　　　　　　c_start=document.cookie.indexOf(key + "=")　　
    		　　　　　　if (c_start!=-1){ 
    		　　　　　　　　c_start=c_start + key.length+1
    		　　　　　　　　c_end=document.cookie.indexOf(";",c_start)
    		　　　　　　　　if (c_end==-1) c_end=document.cookie.length　　
    		　　　　　　　　return unescape(document.cookie.substring(c_start,c_end))
    		　　　　　　}
    		　　　　}
    		　　　　return undefined;
    		}
    		
    		this.updateMessager=function(options){
    			messenger.update(options);
    		}
    		
    		this.showProgress=function(){
    			
    			messenger=Messenger.post({
      		       message: "<span style=\"font-size: larger;\" id='upload_msg_"+$attrs.id+"'>文件准备开始上传，请稍等...</span>",
      		      hideAfter:false,
      		       actions: {
      		        btnStop: {
      		          label: "<span id='op'>暂停<span>",
      		          action: function(event,b){
      		        	var textSpan=$(event.currentTarget).find("span#op");
      		        	if("暂停"==textSpan.text()){
      		        		stop();
      		        		textSpan.text("继续");
      		        	}else if("继续"==textSpan.text()){
      		        		retry();
      		        		textSpan.text("暂停");
      		        	}
      		          }
      		        },
      		        btnCancel: {
    		          label: '取消',
    		          action: function(a,b){
    		        	 cancel();
    		          }
        		    }
      		      }
      		    });
    		}
    		
    		this.hideProgress=function(){
    			if(undefined!=messenger){
    				messenger.hide();
    			}
    			this.hideLoadingDiv();
    		}
    		
    		var stop=function(){    			
    			$scope.chunkUpLoader.stop(true);
    		}
    		this.stop=stop;
    		
    		var retry=function(){
    			$scope.chunkUpLoader.retry();
    		}
    		this.retry=retry;
    		
    		var cancel=function(){
    			$scope.chunkUpLoader.stop(true);
    			if(undefined!=messenger){
    				messenger.hide();
    			}
    			var groupDom=form.mainForm.getDom().find("#formGroup-"+$attrs.id);
    			groupDom.find(".message-loading-overlay").remove();
    		}
    		this.cancel=cancel;
    		
    		this.abort=function(){
    			$scope.chunkUpLoader.stop(true);
    			this.hideProgress();
    		};
    		
    		this.reset=function(){
    			$element.find(".ace-file-input .ace-file-container").removeClass("selected");
    			$($element.find(".ace-file-name .ace-icon")[0]).removeClass("fa-file");
    			$($element.find(".ace-file-name .ace-icon")[0]).addClass("fa-upload");
    			$($element.find(".ace-file-name")[0]).attr("data-title","未选中文件 ...");
    			$scope.chunkUpLoader.reset();
    		}
    		
    		this.getValue=function(){
    			return $scope.chunkUpLoader.getFiles()[0];
    		}
    		
    		
    		//自定义验证黑名单
    		this.validateDeny=function(file){
    			
    			var rExt = /\.\w+$/;
    			if(!$scope.deny){
    				return true;
    			}
    			
    			var accept=undefined;
    			try{
    				var denyArray=angular.fromJson($scope.deny);
    				 accept = '\\.' + denyArray.join(',')
                    .replace( /,/g, '$|\\.' )
                    .replace( /\*/g, '.*' ) + '$';
                    accept = new RegExp( accept, 'i' );
    			}catch(e){}
    			
                var invalid = !file || !file.size || accept &&
                rExt.exec( file.name ) && !accept.test( file.name );
                
                return invalid;
    		}
    		
    	},
    	link:function(scope, element, attrs,controller){
    		
    		var promise = $ocLazyLoad.load(['ext/widget/file_input_chunk/lib/webuploader.js', 'ext/widget/file_input_chunk/lib/webuploader.css' ]);
			promise.then(function() {
					

					$(function() {
						// 检测是否已经安装flash，检测flash的版本
						flashVersion = ( function() {
						    var version;
						
						    try {
						        version = navigator.plugins[ 'Shockwave Flash' ];
						        version = version.description;
						    } catch ( ex ) {
						        try {
						            version = new ActiveXObject('ShockwaveFlash.ShockwaveFlash')
						                    .GetVariable('$version');
						        } catch ( ex2 ) {
						            version = '0.0';
						        }
						    }
						    version = version.match( /\d+/g );
						    return parseFloat( version[ 0 ] + '.' + version[ 1 ], 10 );
						} )();
						
						if ( !WebUploader.Uploader.support('flash') && WebUploader.browser.ie ) {
						
						    // flash 安装了但是版本过低。
						    if (flashVersion) {
						        (function(container) {
						            window['expressinstallcallback'] = function( state ) {
						                switch(state) {
						                    case 'Download.Cancelled':
						                        alert('您取消了更新！')
						                        break;
						
						                    case 'Download.Failed':
						                        alert('安装失败')
						                        break;
						
						                    default:
						                        alert('安装已成功，请刷新！');
						                        break;
						                }
						                delete window['expressinstallcallback'];
						            };
						
						            var swf = 'ext/widget/file_input_chunk/lib/expressInstall.swf';
						            // insert flash object
						            var html = '<object type="application/' +
						                    'x-shockwave-flash" data="' +  swf + '" ';
						
						            if (WebUploader.browser.ie) {
						                html += 'classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" ';
						            }
						
						            html += 'width="100%" height="100%" style="outline:0">'  +
						                '<param name="movie" value="' + swf + '" />' +
						                '<param name="wmode" value="transparent" />' +
						                '<param name="allowscriptaccess" value="always" />' +
						            '</object>';
						
						            container.html(html);
						
						        })(element);
						
						    // 压根就没有安转。
						    } else {
						    	element.html('<a href="http://www.adobe.com/go/getflashplayer" target="_blank" border="0"><img alt="get flash player" src="http://www.adobe.com/macromedia/style_guide/images/160x41_Get_Flash_Player.jpg" /></a>');
						    }
						
						    return;
						} else if (!WebUploader.Uploader.support()) {
						    alert( 'Web Uploader 不支持您的浏览器！');
						    return;
						}
						
						WebUploader.Uploader.unRegister("chunkBSF");
		    			WebUploader.Uploader.register({
		    			    'before-send-file': 'preupload',
		    			    'name':'chunkBSF'
		    			}, {
		    				preupload: function(file,arg1,arg2){
		    			        var me = this,
		    			            owner = this.owner,
		    			            server = me.options.server,
		    			            deferred = WebUploader.Deferred();
		    			        
		    		            var start =  +new Date();
		    		            
		    		            // 返回的是 promise 对象
		    		            owner.md5File(file, 0, 1 * 1024 * 1024).then(function(ret) {
		    		                    // console.log('md5:', ret);
		    		                    var end = +new Date();
		    		                    
		    		                    file.md5Str=ret;
		    		                    
		    		                    console.log('HTML5: md5 ' + file.name + ' cost ' + (end - start) + 'ms get value: ' + ret);
		    		                    
		    			    			//上传前cookie判断此文件是否有过断点记录
		    			    			var cookieUid=controller.getCookie(attrs.id+"_"+ret);
		    			    			if(!cookieUid){
		    			    				cookieUid=WebUploader.Base.guid();
		    			    			}
		    			    			
		    			    			owner.option("formData",{guid:cookieUid});
		    			    			
		    			    			//不分片，直接返回
		    		                    if("true"!=attrs.chunked){
		    		                    	deferred.resolve();
		    		                    	return;
		    		                    }
		    		                    
		    	    			        //与服务安验证
				    			        $.ajax("chunkfile/"+attrs.process+"/getChunksByGuidAndName",{
				    			            dataType: 'json',
				    			            data: {
				    			                guid:cookieUid,
				    			                fileName:file.name,
				    			                md5:ret,
				    			                _t:Math.random()
				    			            },
				    			            success: function( response ){
				    			            	scope.uploadeChunks=response.chunks;
				    			       			owner.chunks=response.chunks;
				    			                deferred.resolve();
				    			            },
				    			            error:function(response){
				    			            	deferred.reject();
				    			            }
				    			        });
		    		            });
		    		            
	
		    			        return deferred.promise();
		    			    }
		    			});
		
		    			WebUploader.Uploader.unRegister("chunkBS");
		    			WebUploader.Uploader.register({
		    			    'before-send': 'preupload',
		    			    'name':'chunkBS'
		    			}, {
		    				preupload: function( file ) {
		    			        var me = this,
		    			            owner = this.owner,
		    			            server = me.options.server,
		    			            deferred = WebUploader.Deferred();
		    					
		    					if($.inArray(file.chunk+"",scope.uploadeChunks)==-1){
		    						deferred.resolve();
		    					}else{
		    						//console.log("跳过分片:"+file.chunk);
		    						deferred.reject();
		    					}
		    			        return deferred.promise();
		    			    }
		    			});
		    			
		    			var options={
				    		    // swf文件路径
				    		    swf: 'ext/widget/file_input_chunk/lib/Uploader.swf',
				    		    // 文件接收服务端。
				    		    server: "chunkfile/"+attrs.process+"/upload",
				    		    //runtimeOrder:'flash',
				    		    chunkSize: 5*1024*1024,
				    		    threads:1,
				    		    chunkRetry:0,
				    		    fileNumLimit:1,
				    		    fileSingleSizeLimit:attrs.maxSize
				    	}
		    			
		    			if("true"==attrs.chunked){
		    				options.chunked=true;
		    			}
		    			
		    			if(scope.allow){
		    				try{
		    					var allowStr=angular.fromJson(scope.allow).join(",");
		    					options.accept={
		    							title:allowStr,
		    							extensions:allowStr
		    					};
		    				}catch(e){}
		    			}
		    			
		    			
			    		var chunkUpLoader = WebUploader.create(options);
			    					    		
			    		scope.chunkUpLoader=chunkUpLoader;
			    		
			    		var btnStr="<label class=\"ace-file-input\" style=\"display: block;\">"+
											"<span class=\"ace-file-container\" data-title=\"浏览...\">"+
											"<span class=\"ace-file-name\" data-title=\"未选中文件 ...\">"+
											"<i class=\"ace-icon fa fa-upload\"></i></span>"+
											"</span>"+
											"<a class=\"remove\" style=\"z-index:1;\" href=\"javascript:void(0);\">"+
												"<i class=\" ace-icon fa fa-times\"></i>"+
											"</a>"+
										"</label>";
			    		
			    		var btn=$("<div class=\"chunk_btn\" style=\"width:100%\"></div>");
			    		
			    		chunkUpLoader.addButton({
						    id: element,
						    button:btn,
						    innerHTML:btnStr,
						    style:false,
						    multiple:false
						});
					
			    		chunkUpLoader.on( 'beforeFileQueued', function( file,arg2,arg3){
			    			chunkUpLoader.reset();
			    		    return true;
			    		});
			    		
			    		chunkUpLoader.on('reset', function( file,arg2,arg3 ){
			    			
			    		});
			    		
			    		//当有文件被添加进队列的时候
			    		chunkUpLoader.on( 'fileQueued', function(file,arg1,arg2){
			    			
			    			controller.hideProgress();
			    			
			    			element.find(".ace-file-input .ace-file-container").addClass("selected");
			    			$(element.find(".ace-file-name")[0]).attr("data-title",file.name);
			    			$(element.find(".ace-file-name .ace-icon")[0]).removeClass("fa-upload");
			    			$(element.find(".ace-file-name .ace-icon")[0]).addClass("fa-file");
			    			$(element.find(".ace-file-input .remove")[0]).on("click",function(){
			    				controller.reset();
			    			});
			    			
		        			if(scope.autoSubmit){
			        			$timeout(function() {
			        				controller.submit();
			        			});
		        			}
			    		});
			    		
			    		chunkUpLoader.on('uploadSuccess', function (file,response) {
			    			
			    		    controller.updateProgress(1);
			    		    
			    		    //已经上传完成
			    		    if(response&&response.data&&response.data.url){

			    		    	controller.hideProgress();
			    		    	
    		     		    	if(angular.isFunction(scope.onSuccess)){
    		     		    		scope.onSuccess({data:response.data, file:file});
    				     		}
			    		    	return;
			    		    }
			    		    
			    		    controller.updateMessager({type:'success',message:'上传完成，等待后台保存处理...',actions:{}});
			    		    
	    			        //合并分片
	    			        $.ajax("chunkfile/"+attrs.process+"/mergeFiles",{
	    			            dataType: 'json',
	    			            data: {
	    			                guid:this.option("formData").guid,
	    			                fileName:file.name,
	    			                _t:Math.random()
	    			            },
	    			            success: function(response,arg1,arg2){
	    			            	controller.hideProgress();
	    		     		    	if(angular.isFunction(scope.onSuccess)){
	    		     		    		scope.onSuccess({data:response.data, file:file});
	    				     		}
	    			            },
	    			            error:function(response){
	    			            	controller.hideProgress();
	    		      		    	if(angular.isFunction(scope.onError)){
	    			      		    	scope.onError({file:file,errmsg:file.statusText});
	    			      		    }
	    			            }
	    			        });
			    		});
			    		
			    		chunkUpLoader.onUploadProgress = function( file, percentage ) {
			    			//console.log("percentage:"+percentage);
			    			controller.updateProgress(percentage);
			    		};
			    		
			    		
			    		chunkUpLoader.on( 'startUpload',function(){
			    			controller.showLoadingDiv();
			    		});
			    		
			    		
			    		chunkUpLoader.on( 'stopUpload',function(){
			    			
			    			controller.hideLoadingDiv();
			    			
			    			//将uuid写入cookie，以便断点续传
			    			var file=this.getFiles()[0];
			    			
			    			if(file.md5Str){
			    				controller.setCookie(attrs.id+"_"+file.md5Str,this.option("formData").guid);
			    			}
			    		});
			    		
			    		
			    		chunkUpLoader.on( 'uploadError',function(f,errmsg){
			    			
			    			controller.hideProgress();
			    			
			    			//将uuid写入cookie，以便断点续传
			    			if(f.md5Str){
			    				controller.setCookie(attrs.id+"_"+f.md5Str,this.option("formData").guid);
			    			}
			    			
		      		    	if(angular.isFunction(scope.onError)){
			      		    	scope.onError({file:f,errmsg:errmsg});
			      		    }
				    		return false;
			    		});
			    		
			    		
			    		chunkUpLoader.on( 'beforeFileQueued',function(file){
			    			
			    			var invalid=controller.validateDeny(file);
	                        
	                        if(!invalid){
		                        var msgStr="不支持上传该类型的文件";
		                        if(scope.allow) msgStr+="，允许"+scope.allow;
		                        if(scope.deny) msgStr+="，禁止"+scope.deny;
	                        	Messenger.error(msgStr);
	                        }
	                        
				    		return invalid;
			    		});
			    		
			    		chunkUpLoader.on('error',function(eType){
			    			
			    			controller.hideProgress();
			    			
			    			if("Q_TYPE_DENIED"==eType){
		                        var msgStr="不支持上传该类型的文件";
		                        if(scope.allow) msgStr+="，允许"+scope.allow;
		                        if(scope.deny) msgStr+="，禁止"+scope.deny;
	                        	Messenger.error(msgStr);
			    			}else if("F_EXCEED_SIZE"==eType){
			    				var maxSize=parseInt(attrs.maxSize);
		    					var sizeLabel=attrs.maxSize+"字节";
		    					if(maxSize>=1024&&maxSize<(1024*1024)){
		    						var sizeLabel=(maxSize/1024)+"KB";
		    					}else if(maxSize>=(1024*1024)){
		    						var sizeLabel=(maxSize/(1024*1024))+"MB";
		    					}
		    					Messenger.error("文件大小不能超过:"+sizeLabel);
			    			}
			    		});
					});
					
					
			});
	    		
    	}
    };
}]);