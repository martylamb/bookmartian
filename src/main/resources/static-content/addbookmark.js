// ==========================================================================
// When the document is fully loaded, load the dynamic elements into the page
// 
// rebuild the tag cloud at the top of the page
$(document).ready(function () {
    var tagColors = new Array();

    $.ajax({
        // The URL for the request
        url: API_QueryTags,

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {

            if ($("#tagcloud").data("results_hash") != stringToIntegerHash(JSON.stringify(json))) {

                // save the hash of the query results for comparison on update
                $("#tagcloud").data("results_hash", stringToIntegerHash(JSON.stringify(json)));

                $("#tagcloud").empty();
                $("#tagcloud").append("<span class='secondary-text-color'>click to add a tag &gt;&nbsp;&nbsp;&nbsp;</span>");
    
                $(json).each(function (index, element) {
                    if (element.name.substr(0, 1) !== ".") {
                        var link = "<a onclick=\"assignTag('" + element.name + "');\" class=tagflow style=color:" + (element.color != "#000000" ? element.color : "") + " id=" + element.name + ">" + element.name + "</a>&nbsp;&nbsp; ";
                        $("#tagcloud").append(link);
                        tagColors[element.name] = element.color;
                    }
                })
            }
        })
        // Code to run if the request fails; the raw request and
        // status codes are passed to the function
        .fail(function (xhr, status, errorThrown) {
            console.log("Error: " + errorThrown);
            console.log("Status: " + status);
            console.dir(xhr);
        })
        // Code to run regardless of success or failure;
        .always(function (xhr, status) {
        });
    });

function assignTag (tag) {
    currentTags = $("#bookmartian_addinputtags").val();

    // only add the tag to the list if it isn't already present
    if (!currentTags.includes(" " + tag + " ")) {
        if (currentTags.endsWith(" ")) {
            $("#bookmartian_addinputtags").val($("#bookmartian_addinputtags").val() + tag + " ");
        } else {
            $("#bookmartian_addinputtags").val($("#bookmartian_addinputtags").val() + " " + tag + " ");
        }
    }
}