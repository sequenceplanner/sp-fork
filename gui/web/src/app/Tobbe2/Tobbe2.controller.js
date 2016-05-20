/**
 * Created by Martin on 2015-11-19.
 */
(function () {
    'use strict';

    angular
      .module('app.Tobbe2')
      .controller('Tobbe2Controller', Tobbe2Controller);

    Tobbe2Controller.$inject = ['$scope', 'dashboardService', 'eventService','spServicesService'];
    /* @ngInject */
    function Tobbe2Controller($scope, dashboardService, eventService,spServicesService) {
        var vm = this;
        var service = {
            connected: false,
            controlServiceID: '',
            state: [],
            resourceTree: [],
            latestMess: {},
            connect: connect,
            execute: execute
        };

        vm.widget = $scope.$parent.$parent.$parent.vm.widget; //lol what

        //functions
        vm.sendOrder = sendOrder;
        
        activate();
        vm.placeyplaceholder = 'Chose operation'
        vm.myFunction = myFunction;
        vm.activate2 = activate2;
        vm.tobbelito = tobbelito
        vm.value = 1;
        vm.debug14 = 0;

        vm.data = {
            resMult: [    
                {
                    name: 'Robot 2',
                    resource: [
                        {id: '127 18 0 1', action: 'Set at position 1'},
                        {id: '127 18 0 2', action: 'Set at position 2'},
                        {id: '127 18 0 3', action: 'Set at position 3'},
                        {id: '127 18 0 4', action: 'Set at position 4'},
                        {id: '127 18 0 5', action: 'Set at position 5'},
                        {id: '127 0 5 true', action: 'Pick at set position'},
                        {id: '127 0 2 true', action: 'Place at elevator 2'},
                        {id: '127 0 6 true', action: 'Place at table'},
                    ],
                    currVal: 'Choose operation'
                },
                {
                    name: 'Robot 3',
                    resource: [
                        {id: '127 18 0 1', action: 'Set at position 1'},
                        {id: '127 18 0 2', action: 'Set at position 2'},
                        {id: '127 18 0 3', action: 'Set at position 3'},
                        {id: '127 18 0 4', action: 'Set at position 4'},
                        {id: '127 18 0 5', action: 'Set at position 5'},
                        {id: '127 0 5 true', action: 'Pick at set position'},
                        {id: '127 0 2 true', action: 'Place at elevator 2'},
                        {id: '127 0 6 true', action: 'Place at table'},
                    ],
                    currVal: 'Choose operation'
                }
            ],
            resSel: [
                {
                    name: 'Mode',
                    resource: [
                        {id: '135 0 SAKNAS true', action: 'Manual'},
                        {id: '135 0 SAKNAS true', action: 'Auto'}
                    ]
                },
                {
                    name: 'Elevator 1',
                    resource: [
                        {id: '135 0 0 true', action: 'Up'},
                        {id: '135 0 1 true', action: 'Down'}
                    ]
                },
                {
                    name: 'Elevator 2',
                    resource: [
                        {id: '140 0 0 true', action: 'Up'},
                        {id: '140 0 1 true', action: 'Down'}
                    ]
                },
                {   
                    name: 'Flexlink',
                    resource: [
                        {id: '139 0 SAKNAS true', action: 'Start'},
                        {id: '139 0 SAKNAS true', action: 'Stop'}
                    ]
                },
                {
                    name: 'Robot 4',
                    resource: [
                        {id: '128 0 2 true', action: 'Home'},
                        {id: '128 0 3 true', action: 'Dodge'}
                    ]

                },
                {
                    name: 'Robot 4',
                    resource: [
                        {id: '132 0 2 true', action: 'Home'},
                        {id: '132 0 3 true', action: 'Dodge'}
                    ]

                }
            ],
            singleShow: [
                {
                    name: 'Sensor 1',
                    id: 'db hej hej',
                    value: 'false'
                }

            ]

        };


        /* When the user clicks on the button,
         toggle between hiding and showing the dropdown content */
        function myFunction() {
            document.getElementById("myDropdown").classList.toggle("show");
        }

        // Close the dropdown menu if the user clicks outside of it
        window.onclick = function(event) {
            if (!event.target.matches('.dropbtn')) {

                var dropdowns = document.getElementsByClassName("dropdown-content");
                var i;
                for (i = 0; i < dropdowns.length; i++) {
                    var openDropdown = dropdowns[i];
                    if (openDropdown.classList.contains('show')) {
                        openDropdown.classList.remove('show');
                    }
                }
            }
        }
   

        function activate2(int) {
            if(int == 1)
                vm.debug14++;
            else
                vm.debug14--;
        }

        function activate() {
            $scope.$on('closeRequest', function () {
                dashboardService.closeWidget(vm.widget.id);
            });
            eventService.addListener('ServiceError', onEvent);
            eventService.addListener('Progress', onEvent);
            eventService.addListener('Response', onEvent);
        }
    
    function onEvent(ev){
      console.log("control service");
      console.log(ev);


      if (!_.has(ev, 'reqID') || ev.reqID !== service.controlServiceID) return;

      if (_.has(ev, 'attributes.theBus')){
        if (ev.attributes.theBus === 'Connected' && ! service.connected){
          sendTo(service.latestMess, 'subscribe');
        }
        service.connected = ev.attributes.theBus === 'Connected'
      }

      if (_.has(ev, 'attributes.state')){
        service.state = ev.attributes.state;
      }
      if (_.has(ev, 'attributes.resourceTree')){
        service.resourceTree = ev.attributes.resourceTree;
      }
    }

    function updateItems(){
      var its = _.filter(itemService.items, function(o){
        return (angular.isDefined(o.id) && angular.isDefined(service.state[o.id]))
      });
      service.itemState = [];
      _.foreach(its, function(o){
        service.itemState.push({'item': o, 'state': service.state[o.id]})
      })
    }

        function onEvent(ev) {
            console.log("SensorGUI Test");
            console.log(ev);

            if (_.has(ev, 'attributes.stateWithName')) {
                //service.stateWithName = ev.attributes.stateWithName;
                if (ev.attributes.stateWithName.name.equals("IH2.mode")) {
                    vm.value = ev.attributes.stateWithName.value;
                }
            }


        }
        function tobbelito(string) {
            console.log(string);
        }

        function sendOrder() {

            var mess = {"data": {getNext: false, "buildOrder": vm.ButtonColour.kub}};
            spServicesService.callService(spServicesService.getService("operatorService"),
                mess,
                function (resp) {
                    if (_.has(resp, 'attributes.result')) {
                        console.log("Hej" + vm.result);
                    }
                }
            )
        }

        function connect(bus, connectionSpecID, resourcesID){
        var mess = {
            'setup': {
                'busIP': bus.ip,
                'publishTopic': bus.publish,
                'subscribeTopic': bus.subscribe
            }
        };
        var conn = {};
        if (angular.isDefined(connectionSpecID)){
            conn.connectionDetails = connectionSpecID
        }
        if (angular.isDefined(resourcesID)){
            conn.resources = resourcesID
        }
        mess.connection = conn;

        sendTo(mess, 'connect').then(function(repl){
            console.log("inside first connection");
            console.log(repl);
            if (messageOK(repl) && _.has(repl, 'reqID')){
                service.controlServiceID = repl.reqID;
            }
        });

        service.latestMess = mess;

        }

        function execute(params) {
            var mess = service.latestMess;
            mess.command = {
                'commandType': 'raw',
                'parameters': params
            };
            spServicesService.callService('OperationControl',{'data':mess});
        }
    
    }
})();
