// url to the bookmartian host
var parser = document.createElement('a');
parser.href = document.getElementById ( "bookmartian_scriptblock" ).getAttribute ( "src" );
var host = parser.protocol + "//" + parser.host;

// function called when the save button is clicked
function saveBookmark() {
    $.post({
        url: host + "/api/bookmark/update",
        data: $("#bookmartian_addform").serialize(),
        headers: {'X-BOOKMARTIAN':'aw yeah'}
    })
        .done(function () {
            console.log("bookmark saved.");
            closeAction();
        })
        .fail(function () {
            console.log("POST failed");
        })
        .always(function (data) {
        });
}

// function called when the close button is clicked AND on a successful save
function closeAction() {
    $('#bookmartian_actionpanel').remove();
}

// embedded javascript executes to insert content into host page
(function () {

    var v = "3.1.0";

    if (window.jQuery === undefined || window.jQuery.fn.jquery < v) {
        var done = false;
        var script = document.createElement("script");
        script.src = host + "/jquery-3.1.0.min.js";
        script.onload = script.onreadystatechange = function () {
            if (!done && (!this.readyState || this.readyState == "loaded" || this.readyState == "complete")) {
                done = true;
                initMyBookmarklet();
            }
        };
        document.getElementsByTagName("head")[0].appendChild(script);
    } else {
        initMyBookmarklet();
    }

    function initMyBookmarklet() {

        // function to retrieve the currently selected text on a page
        (window.myBookmarklet = function () {
            function getSelText() {
                var s = '';
                if (window.getSelection) {
                    s = window.getSelection();
                } else if (document.getSelection) {
                    s = document.getSelection();
                } else if (document.selection) {
                    s = document.selection.createRange().text;
                }
                return s;
            }

            if ($("#bookmartian_actionpanel").length == 0) {
                var s = "";
                s = getSelText();
                $('head').append('<link rel="stylesheet" type="text/css" href="' + host + '/style.bookmarklet.css">');
                $("body").append(`
                            <div class="bookmartian_action" id="bookmartian_actionpanel">
                                <div id="bookmartian_addpanel" class="bookmartian_addpanel">
                                    <h1>Add a bookmark</h1>
                                    <form id="bookmartian_addform">
                                        <div class="bookmartian_titleinputpair">
                                            <div class="bookmartian_fieldtitle">title:</div>
                                            <input type="text" name="title" id="bookmartian_addinputtitle" class="bookmartian_addinputtitle" />
                                        </div>
                                        <div class="bookmartian_titleinputpair">
                                            <div class="bookmartian_fieldtitle">url:</div>
                                            <input type="text" name="url" id="bookmartian_addinputurl" class="bookmartian_addinputurl" />
                                        </div>
                                        <div class="bookmartian_titleinputpair">
                                            <div class="bookmartian_fieldtitle">tags:</div>
                                            <input type="text" name="tags" id="bookmartian_addinputtags" class="bookmartian_addinputtags" />
                                        </div>
                                        <div class="bookmartian_titleinputpair">
                                            <div class="bookmartian_fieldtitle">notes:</div>
                                            <input type="text" name="notes" id="bookmartian_addinputnotes" class="bookmartian_addinputnotes" />
                                        </div>
                                        <div class="bookmartian_titleinputpair">
                                            <div class="bookmartian_fieldtitle">&nbsp;</div>
                                            <input type="button" value="save" class="bookmartian_actionbutton" onclick="saveBookmark();" />
                                            <input type="button" value="cancel" class="bookmartian_actionbutton" onclick="closeAction();" />
                                        </div>
                                    </form>
                                </div>
    					    </div> `);
            }
            $('#bookmartian_addinputtitle').val(document.title);
            $('#bookmartian_addinputurl').val(document.location);
            $('#bookmartian_addinputnotes').val(s);
        })();
    }
})();