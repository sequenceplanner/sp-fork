'use strict';

/**
 * @ngdoc directive
 * @name spGuiApp.directive:sop
 * @description
 * # sop
 */
angular.module('spGuiApp')
.directive('oplist', ['$rootScope', 'spTalker', function ($rootScope, spTalker) {



  return {
    template: '<div><p>Operations</p><ul><li style="float:none;" ng-repeat="op in operations.ids">name: {{op.name}}, id: {{op.id}}, version: {{op.version}}</li></ul>' +
              '<p>Operation by ID, requesting \"551d93a2-3bbc-487c-847c-73974dbf7aaa\"</p><ul><li style="float:none;" ng-repeat="op in operation.ids">name: {{op.name}}, id: {{op.id}}, version: {{op.version}}</li></ul>' +
              '<p>Models</p><ul><li style="float:none;" ng-repeat="m in models">model: {{m.model}}, version: {{m.version}}, prevVersion: {{m.version}}</li></ul>' +
              '<p>Model by name, requesting \"model1\"</p><ul><li style="float:none;" ng-repeat="m in models">model: {{m.model}}, version: {{m.version}}, prevVersion: {{m.version}}</li></ul>' +
              '<p>Things</p><ul><li style="float:none;" ng-repeat="thing in things.ids" ng-bind="thing.name"></li></ul>' +
              '<p>Thing by ID</p><ul><li style="float:none;" ng-repeat="theonlything in thing.ids" ng-bind="theonlything.name"></li></ul>' +
              '<button type="button" class="btn btn-default" ng-click="createOp()"><span class="glyphicon glyphicon-asterisk"></span> New op</button>' +
              '<button type="button" class="btn btn-default" ng-click="loadData()"><span class="glyphicon glyphicon-refresh"></span> Refresh</button></div>',
    restrict: 'E',
    scope: {},
    link: function postLink(scope, element, attrs) {

      scope.loadData = function() {

        scope.operations = spTalker.operations.get({model: 'model1'});

        scope.operation = spTalker.operations.get({model: 'model1', op: '551d93a2-3bbc-487c-847c-73974dbf7aaa'});

        scope.models = spTalker.models.query();

        scope.model = spTalker.models.get({model: 'model1'});

        scope.things = spTalker.things.get({model: 'model1'});

        scope.thing = spTalker.things.get({model: 'model1'});
      };

      scope.createOp = function() {
        var opData = {
          "modelVersion": 1,
          "items": [
            {"name": "Op " + Math.floor(Math.random()*1000), "conditions": [], "attributes": {}, "type": "Operation"}
          ]
        };

        spTalker.operations.save({model:'model1'}, opData);
      };

    }
  };
}]);
