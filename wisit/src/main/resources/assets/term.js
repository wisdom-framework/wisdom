
function Wisit(url,selector){
    var URL = typeof url !== "string" ? "/wisit/command" : url;
    var SELECTOR = typeof selector !== "string" ? "#wisit" : selector;

    var self = this;

    var terminal = undefined;
    var commands = {};
    var options = {
        login: false,
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

        $.get(URL, function(commandList){
            commandList.map(function(command){
                commands[command] = function(){
                    var term = this; //this, is the terminal here
                    var args = Array.prototype.slice.call(arguments);

                    $.ajax({
                        url: URL + "/" + command,
                        type: "POST",
                        contentType: "application/json",
                        data : JSON.stringify(args.join(" "))
                    }).done(function(data){
                        if(data.content !== null){
                            term.echo(data.content);
                        }
                        if(data.err !== null){
                            term.error(data.err);
                        }
                    });
                };
            });

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
            socket.onmessage = function(event){self.echo(event.data)};
            socket.onclose = function(){self.echo("Socket closed")};
        } else {
            alert("Your browser does not support Web Socket.");
        }
    }

    function createTerminal(){
        $(SELECTOR).terminal(commands, options);
    }

    self.init = function(){
        handleWSStream();
        populate(commands);
        createTerminal();
    };

    self.echo = function(content){
        if (typeof terminal === "undefined" || terminal === null ){
            terminal = $.terminal.active();
        }
        terminal.echo(content);
    };
}

jQuery(document).ready(function($) {
    new Wisit().init();
});
