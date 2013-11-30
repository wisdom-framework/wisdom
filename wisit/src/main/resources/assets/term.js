



function Wisit(url,selector){
    var URL = typeof url !== "string" ? "/wisit" : url;
    var SELECTOR = typeof selector !== "string" ? "#wisit" : selector;

    function login(login,passwd,callback){
        var auth = {user: login, pass: passwd};

        $.ajax({
            url: URL + "/login",
            type: "POST",
            contentType: "application/json",
            data :  JSON.stringify(auth),
            async : false,
            statusCode : {
                401 : function(){callback(false);},
                200 : function(token){callback(token);}
            }
            }).fail(function(xhr,status,error){
                self.echo(status);
            });
    }

    function logout(){
        //logout
        $.get(URL + "/logout", function(){
            self.echo("Good bye!")
        });

        //destroy the socket
        if(socket !== null){
            socket.close();
            socket = null;
        }

        //clear commands
        commands = {};
    }

    function init(){
        handleWSStream();
        for(var i=0;i<1000000;i++){}
        populate(commands);
    }

    var self = this;

    var terminal = undefined;
    var commands = {};
    var socket = null;
    var options = {
        login: login,
        onInit: init,
        onExit: logout,
        greetings: "You are authenticated",
        width: "100%",
        height: "100%",
        checkArity: false,
        prompt: "wisit>",
        onBlur: function() {
            // the height of the body is only 2 lines initialy
            return false;
        },
        tabcompletion : false
    };


    function populate(commands) {
        $.get(URL + "/command", function(commandList){

            commandList.map(function(command){
                commands[command] = function(){
                    var term = this; //this, is the terminal here
                    var args = Array.prototype.slice.call(arguments);

                    $.ajax({
                        url: URL + "/command/" + command,
                        type: "POST",
                        contentType: "application/json",
                        data : JSON.stringify(args.join(" "))
                    }).done(function(data){
                        if(data.result !== null){
                            term.echo(data.result);
                        }
                        if(data.err !== null){
                            term.error(data.err);
                        }
                    });
                };
            });

            commands["exit"] = function(){
                logout();
            };

            self.echo("Command list loaded");
        });

    }

    function handleWSStream(){
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        if (window.WebSocket) {
            socket = new WebSocket("ws://"+window.location.host+"/wisit/stream");
            socket.onopen = function(){self.echo("Socket opened")};
            socket.onmessage = function(event){
                console.log(event.data);
                var data = JSON.parse(event.data);
                if(typeof data.result != "string"){
                    self.echo(data.result);
                }
                if(typeof data.err != "string"){
                    self.error(data.err);
                }
            };
            socket.onclose = function(){self.echo("Socket closed")};
        } else {
            alert("Your browser does not support Web Socket.");
        }
    }

    function createTerminal(){
        $(SELECTOR).terminal(commands, options);
    }

    function getTerminal(){
        if (typeof terminal === "undefined" || terminal === null ){
            terminal = $.terminal.active();
        }

        return terminal;
    }


    self.start = function(){
        createTerminal();
    };

    self.echo = function(content){
        getTerminal().echo(content);
    };

    self.error = function(content){
        getTerminal().error(content);
    };
}

jQuery(document).ready(function($) {
    new Wisit().start();
});
