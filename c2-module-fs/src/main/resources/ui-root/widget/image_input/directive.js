directives.directive('c2ImageInput', ['$rootScope','FormContainerFactory','$upload','Messenger','$timeout',
                                     function ($rootScope,FormContainerFactory,$upload,Messenger,$timeout) {
return {
	scope:{
		binding:'='
	},
	compile:function($element,$attrs){
		var maxSize=$attrs.maxSize;
		var submit_url=$attrs.targetUrl;
		var id=$attrs.id;
		
		return function($scope,$element,$attrs){
			var formScope=FormContainerFactory.findFormScope($scope);
			try {//ie8 throws some harmless exceptions, so let's catch'em
				//first let's add a fake appendChild method for Image element for browsers that have a problem with this
				//because editable plugin calls appendChild, and it causes errors on IE at unpredicted points
				try {
					document.createElement('IMG').appendChild(document.createElement('B'));
				} catch(e) {
					Image.prototype.appendChild = function(el){}
				}
				
				formScope.$state[id]={imgUrl:"ext/widget/image_input/img/noimage.gif"};
				$scope.$watch('binding',function(value){
					if(angular.isDefined(value)){
						if(value.indexOf('http://')==0){
							formScope.$state[id].imgUrl=value;
						}else{
							formScope.$state[id].imgUrl=submit_url+"/"+value;
						}
					}
				});
				var $img=$element.children("img").first();
				
				$img.editable({
					type: 'image',
					name: 'file',
					value: null,
					image: {
						//specify ace file input plugin's options here
						btn_choose: '选择图片',
						maxSize: maxSize, 

						//and a few extra ones here
						name: 'file',//put the field name here as well, will be used inside the custom plugin
						on_error : function(error_type) {//on_error function will be called when the selected file has a problem
							if(error_type == 1) {//file format error
								Messenger.error("文件不是图片，请选择jpg|gif|png格式的文件")
							} else if(error_type == 2) {//file size rror
								Messenger.error("图片文件太大，只支持上传10M一下的图片");
							} else {//other error
							}
						},
						on_success : function() {
							alert('文件上传成功')
						}
					},
				    url: function(params) {
						// ***UPDATE AVATAR HERE*** //
						//for a working upload example you can replace the contents of this function with 
						//examples/profile-avatar-update.js

						var deferred = null;

						//if value is empty (""), it means no valid files were selected
						//but it may still be submitted by x-editable plugin
						//because "" (empty string) is different from previous non-empty value whatever it was
						//so we return just here to prevent problems
						var value = $img.next().find('input[type=hidden]:eq(0)').val();
						if(!value || value.length == 0) {
							deferred = new $.Deferred
							deferred.resolve();
							return deferred.promise();
						}
						
						var $form = $img.next().find('.editableform:eq(0)')
						var file_input = $form.find('input[type=file]:eq(0)');
						var pk = $img.attr('data-pk');//primary key to be sent to server

						if( "FormData" in window ) {
							var formData_object = new FormData();//create empty FormData object
							
							//serialize our form (which excludes file inputs)
							$.each($form.serializeArray(), function(i, item) {
								//add them one by one to our FormData 
								formData_object.append(item.name, item.value);							
							});
							//and then add files
							$form.find('input[type=file]').each(function(){
								var field_name = $(this).attr('name');
								var files = $(this).data('ace_input_files');
								if(files && files.length > 0) {
									formData_object.append(field_name, files[0]);
								}
							});

							deferred = $.ajax({
										url: submit_url,
									   type: 'POST',
								processData: false,//important
								contentType: false,//important
								   dataType: 'text',//server response type
									   data: formData_object
							})
						}
						else {
							deferred = new $.Deferred

							var temporary_iframe_id = 'temporary-iframe-'+(new Date()).getTime()+'-'+(parseInt(Math.random()*1000));
							var temp_iframe = 
									$('<iframe id="'+temporary_iframe_id+'" name="'+temporary_iframe_id+'" \
									frameborder="0" width="0" height="0" src="about:blank"\
									style="position:absolute; z-index:-1; visibility: hidden;"></iframe>')
									.insertAfter($form);
									
							$form.append('<input type="hidden" name="temporary-iframe-id" value="'+temporary_iframe_id+'" />');
							
							temp_iframe.data('deferrer' , deferred);
							//we save the deferred object to the iframe and in our server side response
							//we use "temporary-iframe-id" to access iframe and its deferred object

							$form.attr({
									  action: submit_url,
									  method: 'POST',
									 enctype: 'multipart/form-data',
									  target: temporary_iframe_id //important
							});

							$form.get(0).submit();
						}

						deferred.done(function(result) {
							$scope.binding=result;
							$scope.$apply();
						}).fail(function(result) {
							alert("There was an error");
						});

						return deferred.promise();
					},
					
					success: function(response, newValue) {
					}
				});
			}catch(e) {};
		};
	}
}

}]);
