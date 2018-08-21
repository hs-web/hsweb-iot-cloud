
var MenuPop = function (menu) {

    var el = $(menu.element);

    //    var sidebar = el.parent();
    //    sidebar.bind('mousewheel', function (event, delta, deltaX, deltaY) {
    //        //        if (window.console && console.log) {
    //        //            console.log(delta, deltaX, deltaY);
    //        //        }
    //        //alert(deltaY);
    //        var offset = 24 * deltaY;
    //        sidebar[0].scrollTop -= offset;
    //    });

    var lostTimer;
    $(document.body).on("mouseenter", ".menu-title", function (event) {
        var jq = $(event.currentTarget);

        if (lostTimer) clearTimeout(lostTimer);

        $(".menu li").removeClass("menu-hover");

        jq.parent().addClass("menu-hover");

        //alert(1);

        //////////////////////////////////////////////////////////

        //top
        var ul = jq.next('ul');
        if (!ul[0]) return;

        ul.css("top", "");

        var viewHeight = $(window).height() - 1;
        var offset = ul.offset(),
            height = ul.outerHeight();

        var top = offset.top;

        if (top + height > viewHeight) {
            top = viewHeight - height;
            ul.offset({ left: 0, top: top });
            ul.css("left", "");
        }

        
    });

    $(document.body).on("mouseleave", ".menu-title", function (event) {
        var jq = $(event.currentTarget);

        lostTimer = setTimeout(function () {
            jq.parent().removeClass("menu-hover");
        }, 200);

    });

}
