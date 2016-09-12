"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var core_1 = require('@angular/core');
var event_bus_service_1 = require("../../core/event-bus.service");
var ItemExplorerNodeComponent = (function () {
    function ItemExplorerNodeComponent(itemService, evBus) {
        var _this = this;
        this.name = "";
        this.expanded = false;
        this.getName = function (id) {
            //TODO null check
            return itemService.getItem(id).name;
        };
        this.sendSelected = function () {
            console.log("VI klickar");
            console.log(_this.node);
            console.log(_this.getName(_this.node.item));
            evBus.tweetToTopic("minTopic", [_this.node.item]);
        };
    }
    ItemExplorerNodeComponent.prototype.ngOnInit = function () {
        this.name = this.getName(this.node.item);
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], ItemExplorerNodeComponent.prototype, "node", void 0);
    ItemExplorerNodeComponent = __decorate([
        core_1.Component({
            selector: 'explorer-node',
            templateUrl: 'app/lazy-widgets/ng2-item-explorer/explorer-node.component.html',
            directives: [ItemExplorerNodeComponent]
        }),
        __param(0, core_1.Inject('itemService')), 
        __metadata('design:paramtypes', [Object, event_bus_service_1.EventBusService])
    ], ItemExplorerNodeComponent);
    return ItemExplorerNodeComponent;
}());
exports.ItemExplorerNodeComponent = ItemExplorerNodeComponent;
//# sourceMappingURL=explorer-node.component.js.map