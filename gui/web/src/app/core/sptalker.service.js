(function () {
    'use strict';

    angular
        .module('app.core')
        .factory('spTalker', spTalker);

    spTalker.$inject = ['$http', '$q', 'logger', 'API'];
    /* @ngInject */
    function spTalker($http, $q, logger, API) {
        var service = {
            getModels: getModels,
            getItems: getItems,
            getRuntimeKinds: getRuntimeKinds,
            getRuntimeInstances: getRuntimeInstances,
            stopRuntimeInstance: stopRuntimeInstance,
            getRegisteredServices: getRegisteredServices,
            postToModelHandler: postToModelHandler,
            postItems: postItems,
            postToRuntimeHandler: postToRuntimeHandler,
            postToRuntimeInstance: postToRuntimeInstance,
            postToServiceHandler: postToServiceHandler,
            postToServiceInstance: postToServiceInstance,
            deleteModel: deleteModel
        };

        return service;

        function getModels() { return getStuff(API.models, 'models'); }
        function getItems(modelID) { return getStuff(API.items(modelID), 'items'); }
        function getRuntimeKinds() { return getStuff(API.runtimeKinds, 'runtime kinds'); }
        function getRuntimeInstances() { return getStuff(API.runtimeHandler, 'runtime instances'); }
        function stopRuntimeInstance(runtimeID) { return getStuff(API.stopRuntimeInstance(runtimeID), 'runtime stop'); }
        function getRegisteredServices() { return getStuff(API.serviceHandler, 'registered services'); }

        function getStuff(restURL, itemKind) {

            return $http.get(restURL)
                .then(success)
                .catch(fail);

            function success(response) {
                return response.data;
            }

            function fail(error) {
                var msg = 'Query for ' + itemKind  + ' failed. ' + error.data.description;
                logger.error(msg);
                return $q.reject(msg);
            }
        }

        function postToModelHandler(data) { return postStuff(API.models, 'model handler', data); }
        function postItems(data, modelID) { return postStuff(API.items(modelID), 'item handler', data); }
        function postToRuntimeHandler(data) { return postStuff(API.runtimeHandler, 'runtime handler', data); }
        function postToRuntimeInstance(data, runtimeID) {
            return postStuff(API.runtimeInstance(runtimeID), 'runtime instance', data);
        }
        function postToServiceHandler(data) { return postStuff(API.serviceHandler, 'service handler', data); }
        function postToServiceInstance(data, serviceID) {
            return postStuff(API.serviceInstance(serviceID), 'service instance', data);
        }

        function postStuff(restURL, receiver, data) {
            return $http.post(restURL, data)
                .then(success)
                .catch(fail);

            function success(response) {
                return response.data;
            }

            function fail(error) {
                var msg = 'Post to ' + receiver  + ' failed. ' + error.data.description;
                logger.error(msg);
                return $q.reject(msg);
            }
        }

        function deleteModel(modelID) { return deleteStuff(API.model(modelID), 'model'); }

        function deleteStuff(restURL, itemKind) {

            return $http.delete(restURL)
                .then(success)
                .catch(fail);

            function success(response) {
                return response.data;
            }

            function fail(error) {
                var msg = 'Deletion of ' + itemKind  + ' failed. ' + error.data.description;
                logger.error(msg);
                return $q.reject(msg);
            }
        }
    }
})();
