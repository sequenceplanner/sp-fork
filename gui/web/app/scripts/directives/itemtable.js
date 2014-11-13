'use strict';

/**
 * @ngdoc directive
 * @name spGuiApp.directive:itemTable
 * @description
 * # itemTable
 */
angular.module('spGuiApp')
  .directive('itemTable', function (itemListSvc, spTalker, RecursionHelper, ITEM_KINDS, itemSvc) {
    return {
      templateUrl: 'views/itemtable.html',
      restrict: 'E',
      scope: {
        parentItem: '=',
        servedItems: '=',
        alterCheckedArray: '=',
        selection: '=',
        attrSelection: '=',
        itemListScope: '='
      },
      controller: function($scope) {
        $scope.itemListSvc = itemListSvc;
        $scope.itemSvc = itemSvc;
        $scope.items = [];
        $scope.spTalker = spTalker;
        $scope.itemKinds = ITEM_KINDS;
        $scope.rowSettings = {};

        $scope.$on('collapseAll', function() {
          angular.forEach($scope.rowSettings, function(rowSetting) {
            rowSetting.edit = false;
            rowSetting.infoIsCollapsed = true;
            rowSetting.expandChildren = false;
          });
        });

        $scope.haveChildren = function(id) {
          var item = spTalker.getItemById(id);
          return item && item.attributes.children && item.attributes.children.length > 0;
        };

        $scope.addItemExpandListener = function(item, row) {
          $scope.$on('show-info-' + item.id, function(event) {
            if(event.defaultPrevented) { // to avoid enter edit mode in two places at one time
              return;
            }
            event.preventDefault();
            row.infoIsCollapsed = false;
            row.edit = true;

          });
          $scope.$on('show-children-' + item.id, function(event) {
            if(event.defaultPrevented) {
              return;
            }
            event.preventDefault();
            itemListSvc.expandChildren(row, false);
          });
        };

        if(typeof $scope.servedItems === 'undefined') {
          if(spTalker.itemsRead) {
            itemListSvc.getChildren($scope.parentItem, $scope.items);
          }
          $scope.$on('itemsQueried', function() {
            itemListSvc.getChildren($scope.parentItem, $scope.items);
          });
        } else {
          $scope.items = $scope.servedItems;
        }

        $scope.filterKey = function(key){
          return key !== 'id' && key !=='name' && key !== 'isa' && key !== 'version' && key !== 'attributes'
        };

        $scope.isAttributesEmpty = function(item) {
          return _.isEmpty(item.attributes)
        };

        $scope.objectify = function(key, value) {
            var obj = {};
            obj[key] = value;
            return obj
        }

      },
      compile: function(element) {
        return RecursionHelper.compile(element);
      }
    };
  });
