importResource("/admin/css/common.css");

window.componentsImport = [
    "components-default"
];
importMiniui(function () {
    mini.parse();
    require(["request", "message", "miniui-tools"], function (request, message, tools) {
        require(["pages/form/designer-drag/parser", "css!pages/form/designer-drag/defaults"], function (Parser) {
            var com = request.getParameter("components");
            if (com) {
                window.componentsImport = com.split(",");
            }
            if (componentsImport.indexOf("components-default") !== -1) {
                require(["components-default"], function () {
                    loadImport()
                })
            } else {
                loadImport();
            }

            function loadImport() {
                require(window.componentsImport, function () {
                    if (window.getConfig) {

                        var parser = window.parser = new Parser(window.getConfig());
                        parser.render($("#preview"));
                        $(".get-form-data").on("click", function () {
                            message.alert(JSON.stringify(parser.getData(false)));
                        })
                    } else {
                        message.showTips("加载错误");
                    }
                });
            }


        });
    });

});