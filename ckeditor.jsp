<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>A Simple Page with CKEditor</title>
        <!-- Make sure the path to CKEditor is correct. -->
        <!--<script src="https://code.jquery.com/jquery-3.1.0.min.js" integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s=" crossorigin="anonymous"></script>-->
        <script src="ckeditor/ckeditor.js"></script>
        <script type="text/javascript" src="ckfinder/ckfinder.js"></script>
        <!--<script src="ckeditor/adapters/jquery.js"></script>-->
    </head>
    
    <body>
        <form action="math.do" method="post">
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
                    extraPlugins: 'uploadimage,image2,mathjax,eqneditor,ckeditor_wiris',
                    //mathjax,texzilla,mathedit,eqneditor,FMathEditor',
                    mathJaxLib: 'http://cdn.mathjax.org/mathjax/2.6-latest/MathJax.js?config=TeX-AMS_HTML',
                    removePlugins: 'image',
                    height:350
                } );
                CKFinder.setupCKEditor( editor ,'/ckeditor/ckfinder') ;
            </script>
        </form>
    </body>
</html>