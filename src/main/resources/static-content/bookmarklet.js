// url to the bookmartian host
var parser = document.createElement('a');
parser.href = document.getElementById("bookmartian_scriptblock").getAttribute("src");
var host = parser.protocol + "//" + parser.host;

// function called when the save button is clicked
function bookmartian_saveBookmark() {

    // do not serialize the oldurl field if no old url was specified or the url did not change
    var serializedFormData = null;
    if ($('#bookmartian_addinputoldUrl').val().length == 0 || $('#bookmartian_addinputoldUrl').val() === $('#bookmartian_addinputurl').val()) {
        serializedFormData = $("#bookmartian_addform :not(#bookmartian_addinputoldUrl)").serialize();
    } else {
        serializedFormData = $("#bookmartian_addform").serialize();
    }

    $.post({
        url: host + "/api/bookmark/update",
        data: serializedFormData,
        headers: { 'X-BOOKMARTIAN': 'aw yeah' }
    })
        .done(function () {
            console.log("bookmark saved.");
            bookmartian_closeAction();
        })
        .fail(function () {
            console.log("POST failed");
        })
        .always(function (data) {
        });
}

// function called when the close button is clicked AND on a successful save
function bookmartian_closeAction() {
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
                    s = window.getSelection().toString();
                } else if (document.getSelection) {
                    s = document.getSelection().toString();
                } else if (document.selection) {
                    s = document.selection.createRange().text;
                }
                return s;
            }

            if ($("#bookmartian_actionpanel").length == 0) {
                // grab any currently selected text on the page to stick in the notes field
                var s = null;
                s = getSelText();

                // grab the text of the body of the page (without scripts and other embedded elements) and clean up extra whitespace
                var bodyclone = $('body').clone();
                bodyclone.find("script, style, img").remove();
                body = bodyclone.text().replace(/\s+/g, ' ');

                $('head').append('<link rel="stylesheet" type="text/css" href="' + host + '/style.bookmarklet.css">');
                $('head').append('<link rel="stylesheet" type="text/css" href="' + host + '/colors.css">');
                $('head').append('<link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet">');
                $("body").append(`
                <div class="bookmartian_action default-primary-color" id="bookmartian_actionpanel">
                <div id="bookmartian_addpanel" class="bookmartian_addpanel bookmartian_primary-text-color">
                    <div class="bookmartian_title">Save this page to bookmartian...</div>
                    <form id="bookmartian_addform">
                        <input type="hidden" name="oldUrl" id="bookmartian_addinputoldUrl" />
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">title</div>
                            <input type="text" name="title" id="bookmartian_addinputtitle" class="bookmartian_field" />
                        </div>
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">url</div>
                            <input type="text" name="url" id="bookmartian_addinputurl" class="bookmartian_field" />
                        </div>
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">tags</div>
                            <input type="text" name="tags" id="bookmartian_addinputtags" class="bookmartian_field" />
                        </div>
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">notes</div>
                            <input type="text" name="notes" id="bookmartian_addinputnotes" class="bookmartian_field" />
                        </div>
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">image url</div>
                            <input type="text" name="imageUrl" id="bookmartian_addinputimageUrl" class="bookmartian_field" />
                        </div>
                        <div class="bookmartian_inputpair">
                            <div class="bookmartian_fieldtitle">&nbsp;</div>
                            <input type="button" value="save" class="bookmartian_actionbutton" onclick="bookmartian_saveBookmark();" />
                            <input type="button" value="cancel" class="bookmartian_actionbutton" onclick="bookmartian_closeAction();" />
                        </div>
                    </form>
                </div>
            </div> `);
            $('#bookmartian_addinputtags').focus();
            }

            // check to see if this url is already bookmarked and, if it is, prepopulate the fields with the saved info
            $.ajax({
                // The URL for the request
                url: host + "/api/bookmark?url=" + escape(document.location),

                headers: { 'X-BOOKMARTIAN': 'aw yeah' },

                // Whether this is a POST or GET request
                type: "GET",

                // The type of data we expect back
                dataType: "json"
            })
                // Code to run if the request succeeds (is done);
                // The response is passed to the function
                .done(function (json) {
                    if (json.status === 'success') {
                        $('#bookmartian_addpanel h1').text('Update a saved bookmark');
                        $('#bookmartian_addinputtitle').val(json.data.title);
                        $('#bookmartian_addinputtags').val(json.data.tags?json.data.tags.toString().replace(/,/g, ' '):'');
                        $('#bookmartian_addinputnotes').val(json.data.notes);
                        $('#bookmartian_addinputoldUrl').val(document.location);
                    } else {
                        $('#bookmartian_addinputtitle').val(document.title);                    
                    }                    
                })
                // Code to run if the request fails; the raw request and
                // status codes are passed to the function
                .fail(function (xhr, status, errorThrown) {
                    $('#bookmartian_addinputtitle').val(document.title);
        
                    console.log("Error: " + errorThrown);
                    console.log("Status: " + status);
                    console.dir(xhr);
                })
                // Code to run regardless of success or failure;
                .always(function (xhr, status) {
                    $('#bookmartian_addinputurl').val(document.location);
                    $('#bookmartian_addinputbody').val(body);
                    if (s) { $('#bookmartian_addinputnotes').val(s); }
                });
        })();
    }
})();