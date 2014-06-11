LoginController = Backbone.Marionette.Controller.extend({

    initialize: function () {

        var self = this;

        // Wire events to functions
        MongoApp.vent.on(MongoApp.events.LOGIN, function (username, password) {
            self.login(username, password);
        });
    },

    showLogin: function(options) {
        this.loginLayout = new LoginLayout();
        options.region.show(this.loginLayout);
    },

    /**
     * Authenticates the user
     *
     * @param username
     * @param password
     */
    login: function(username, password) {

        var self = this;

        $.ajax({
            type: "POST",
            url: "/securityuserapp/api/login",
            data: {username: username, password: password, appkey: 'VcfMiner'},
            dataType: "json",
            success: function(json) {
                switch (json.Status) {
                    case 'OK':
                        console.log("login successful");
                        self.initUser(username, json.UserToken);
                        break;
                    default:
                        console.log("login failed");
                }
            },
            error: function(jqXHR) {
                MongoApp.vent.trigger(MongoApp.events.ERROR, jqXHR.responseText);
            }
        });
    },

    /**
     * Fetches information about the user, initializes the Backbone model, and
     * delegates further action by firing a LOGIN_SUCCESS event.
     *
     * @param username
     * @param userToken
     */
    initUser: function(username, userToken) {

        $.ajax({
            type: "POST",
            url: "/securityuserapp/api/users/" + username,
            headers: {usertoken: userToken},
            data: {},
            dataType: "json",
            success: function(json) {

                // init Backbone model
                var user = new User(json.User);

                // add the authentication token
                user.set("token", userToken);

                MongoApp.vent.trigger(MongoApp.events.LOGIN_SUCCESS, user);
            },
            error: function(jqXHR ) {
                MongoApp.vent.trigger(MongoApp.events.ERROR, jqXHR.responseText);
            }
        });
    }

});
