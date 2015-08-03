(function () {
    'use strict';

    angular
        .module('app.core')
        .factory('eventHandler', eventHandler);

    eventHandler.$inject = ['$rootScope', 'API', 'logger'];
    /* @ngInject */
    function eventHandler($rootScope, API, logger) {
        var service = {
            addListener: addListener,
            eventSource: null
        };

        return service;

        /* global EventSource */
        function createEventSource() {
            if (typeof(EventSource) !== 'undefined') {
                service.eventSource = new EventSource(API.events);
                $rootScope.$on('$destroy', function () {
                    service.eventSource.close();
                });
            } else {
                logger.error('Your browser does\'nt support SSE. Please update your browser.');
            }
        }

        function addListener(target, handlerFunc) {
            if (service.eventSource === null) {
                createEventSource();
                /*logger.error('Couldn\'t add an SSE listener for target ' + target + ' because there\'s no ' +
                    'EventSource to add it to.');*/
            }
            service.eventSource.addEventListener(target, function(e) {
                var data = angular.fromJson(e.data);
                logger.info('Received ' + data.event + ' event for target ' + target + '.');
                $rootScope.$apply(handlerFunc(data));
            });
            logger.info('Added a SSE listener for target ' + target + '.');
        }

    }
})();
