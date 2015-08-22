$(function () {

    var activeChat;

    var NickForm = function () {
        var $addForm = $("#nickForm");

        $addForm.submit(function (ev) {
            $.ajax({
                type: $addForm.attr('method'),
                url: $addForm.attr('action'),
                data: $addForm.serialize(),
                success: function () {
                    console.log("nick form sent");
                }
            });
            ev.preventDefault();
        });
    };

    var RoomsList = function () {

        var rooms = [];

        var roomsContent = {};

        var $el = $("#rooms");

        var $addForm = $("#addRoomForm");
        $addForm.submit(function (ev) {
            $.ajax({
                type: $addForm.attr('method'),
                url: $addForm.attr('action'),
                data: $addForm.serialize(),
                success: function () {
                    console.log("form sent");
                }
            });
            ev.preventDefault();
        });

        var roomsTemplate = function (rooms) {
            return _.reduce(rooms, function (content, room) {
                    return content + "<li data-id=/1>".replace("/1", room.id) + room.name + "</li>";
                }, "<ul>") + "</ul>";
        };

        this.render = function () {
            $el.html(roomsTemplate(rooms));
            $el.find("li").click(function (e) {
                var id = $(e.target).data("id");
                if (activeChat) {
                    roomsContent[activeChat.id] = activeChat.close();
                }
                activeChat = new ChatWindow(id, roomsContent[id]);
            });
        };

        this.add = function (room) {
            rooms.push(room);
            this.render();
        };
    };

    var ConnectionLabel = function () {
        var $connected = $("#connLabel");
        var $disconnected = $("#disconnected");

        this.set = function (text) {
            $disconnected.hide();
            $connected.text(text).show();
        };

        this.disconnected = function () {
            $connected.hide();
            $disconnected.show();
        };
    };

    var ChatWindow = function (id, content) {
        this.id = id;

        var $el = $("#chat");

        if (content) {
            $el.html(content);
        }

        var postForm = new PostForm(id);

        var chatFeed = new EventSource("/chatFeed/" + id);

        chatFeed.onopen = function () {
            console.log("OPENING for chat: " + id);
        };

        var addMessage = function (message) {
            $el.append("<p>" + message.sender + "</p>");
            $el.append("<p>" + message.content + "</p>");
        };

        chatFeed.onmessage = function (event) {
            console.log("Received chat message");
            addMessage(JSON.parse(event.data));
        };

        this.close = function () {
            var content = $el.html();
            $el.empty();
            postForm.close();
            chatFeed.close();
            return content;
        };
    };

    var PostForm = function (id) {
        var $messageFormWrapper = $("#sendMessage");
        $messageFormWrapper.html($("#sendMessageTemplate").html());
        var $form = $messageFormWrapper.find("form");

        $form.removeClass("hide");

        $form.submit(function (ev) {
            $.ajax({
                type: $form.attr('method'),
                url: $form.attr('action') + id,
                data: $form.serialize(),
                success: function () {
                    console.log("post sent");
                }
            });
            ev.preventDefault();
        });

        this.close = function () {
            $messageFormWrapper.empty();
        };
    };

    new NickForm();

    var roomsList = new RoomsList();

    var connectionLabel = new ConnectionLabel();

    var source = new EventSource("/roomsFeed");
    source.onmessage = function (event) {
        console.log(event);
        if (event.data.startsWith("Connected to")) {
            connectionLabel.set(event.data);
        } else {
            roomsList.add(JSON.parse(event.data));
        }
    };

    source.onopen = function () {
        console.log("OPENING");
    };

    source.onerror = function () {
        connectionLabel.disconnected();
    };
});