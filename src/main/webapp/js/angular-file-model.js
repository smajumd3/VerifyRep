/*!
 * @module angularjs-file-model
 * @description AngularJS directive to support NgModel for HTML input file types
 * @version v1.0.4
 * @link https://github.com/Sibiraj-S/angularjs-file-model
 * @licence MIT License, https://opensource.org/licenses/MIT
 */
(function(){"use strict";angular.module("angularjsFileModel",[]).directive("fileModel",function(){return{require:"ngModel",link:function(e,i,l,t){i.on("change",function(e){var a;a=[],void 0!==l.asFile?a=i[0].files:angular.forEach(i[0].files,function(e){var i;i={name:e.name,size:e.size,type:e.type,lastModified:e.lastModified,lastModifiedDate:e.lastModifiedDate,url:URL.createObjectURL(e),_file:e},a.push(i)}),t.$setViewValue(a)})}}})}).call(this);
//# sourceMappingURL=fileModel.min.js.map