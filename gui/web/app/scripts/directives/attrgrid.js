'use strict';

/**
 * @ngdoc directive
 * @name spGuiApp.directive:attrGrid
 * @description
 * # attrGrid
 */
angular.module('spGuiApp')
  .directive('attrGrid', function (RecursionHelper) {
    return {
      restrict: 'E',
      scope: {
        attrObj : '=',
        edit: '=',
        attributeTypes: '='
      },
      templateUrl: 'views/attrgrid.html',
      controller: function($scope) {
        $scope.isEmpty = function (obj) {
          return _.isEmpty(obj)
        };

        if(typeof $scope.attrObj === 'undefined') {
          $scope.attrObj = {};
        }

        $scope.isArray = function(){
          var res = angular.isArray($scope.attrObj)
          return res
        }


        $scope.getType = function(obj) {
          if(obj instanceof Date) {
            return 'date';
          }
          if (typeof obj == 'string' &&
            /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(obj)){
            return 'item'
          }
          if (angular.isDefined(obj.emptyItem)){
            return 'item'
          }

          return typeof obj;
        };


        $scope.deleteObjProp = function(obj, prop) {
          delete obj[prop];
        };
      },
      compile: function(element) {
        // Use the compile function from the RecursionHelper,
        // And return the linking function(s) which it returns

        return RecursionHelper.compile(element);
      }

    };
  });
