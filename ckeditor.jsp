<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>A Simple Page with CKEditor</title>
        <!-- Make sure the path to CKEditor is correct. -->
        <script src="ckeditor/ckeditor.js"></script>
        <script type="text/javascript" src="ckfinder/ckfinder.js"></script>
    </head>
    
    <body>
        <form>
            <textarea name="editor1" id="editor1" rows="10" cols="80">
                This is my textarea to be replaced with CKEditor.
            </textarea>
            <script>
                // Replace the <textarea id="editor1"> with a CKEditor
                // instance, using default configuration.
                CKEDITOR.editorConfig =  function (config) {  
				    config.filebrowserBrowseUrl =  'ckfinder.html' ;  
				    config.filebrowserImageBrowseUrl =  'ckfinder.html?type=Images' ;  
				    config.filebrowserFlashBrowseUrl =  'ckfinder.html?type=Flash' ;  
				    config.filebrowserUploadUrl =  'core/connector/java/connector.java?command=QuickUpload&type=Files' ;  
				    config.filebrowserImageUploadUrl =  'core/connector/java/connector.java?command=QuickUpload&type=Images' ;  
				    config.filebrowserFlashUploadUrl =  'core/connector/java/connector.java?command=QuickUpload&type=Flash' ;  
				    config.filebrowserWindowWidth = '1000';  
				    config.filebrowserWindowHeight = '700';  
				    config.language =  "zh-cn" ;  
				}; 
				var editor = CKEDITOR.replace( 'editor1', {
					extraPlugins: 'uploadimage,image2',
					removePlugins: 'image',
					height:350
				} );
                CKFinder.setupCKEditor( editor ,'/ckeditor/ckfinder') ;
            </script>
        </form>
    </body>
</html>