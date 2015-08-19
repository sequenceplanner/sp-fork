(function () {
    'use strict';

    angular
        .module('app.core', [
            'ngAnimate', 'ngSanitize', 'gridster', 'ngStorage', 'ng.jsoneditor', 'ngJsTree',
            'blocks.exception', 'blocks.logger', 'blocks.router', 'mwl.confirm',
            'ui.router', 'smart-table', 'xeditable', 'ui.bootstrap', 'angular.filter'
        ])
        .run(runBlock);

    runBlock.$inject = ['inlineEditingService'];

    function runBlock(inlineEditingService) {
        inlineEditingService.initialize();
    }

})();