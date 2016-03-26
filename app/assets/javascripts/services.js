
/** chatModel service, provides chat rooms (could as well be loaded from server) */
angular.module('sseChat.services', []).service('chatModel', function () {
    var getRooms = function () {
        return [

            {name: 'User 1 Key 1', value: 'a224c11a-e145-4011-a48d-0e191fac1a4b'},
            {name: 'User 1 Key 2', value: '779e1381-0a40-47bd-9326-42ad438f8a54'},
            {name: 'User 2 Key 1', value: 'f83832f0-6dd3-4057-8629-2421f7e6d80e'},
            {name: 'User 2 Key 2', value: '362e4f12-3865-4ec1-80a2-1d6e98181944'}





        ];
    };
    return { getRooms: getRooms };
});