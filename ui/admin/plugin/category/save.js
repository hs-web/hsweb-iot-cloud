importResource("/admin/css/common.css");
require(["css!pages/form/designer-drag/defaults"]);
var scriptLanguage = [
    {id: "sql"},
    {id: "javascript"},
    {id: "groovy"}
]
importMiniui(function () {
    mini.parse();
    var mainForm;



    require(["request", 'pages/form/designer-drag/parser'], function (request, FormParser) {


        var dataId = request.getParameter("id");
        var readOnly = request.getParameter("readOnly");
        require(["text!save.json", "pages/form/designer-drag/components-default"], function (config) {
            mainForm = new FormParser(JSON.parse(config));
            mainForm.render($("#basic-info"));

            if (dataId) loadData(dataId);
            if (readOnly) {
                $(".save-button").hide();
                mainForm.setReadOnly(true);
            }
        });


        $(".save-button").on("click", (function () {
            require(["message"], function (message) {
                var data = getData(true);
                if (!data) {
                    return;
                }
                if (dataId) {
                    data.id = dataId;
                }
                var loading = message.loading("提交中");

                request.patch('plugin-category', data, function (response) {
                    loading.hide();
                    if (response.status === 200) {
                        dataId = response.result;
                        message.showTips("提交成功")
                    } else {
                        mini.alert(response.message);
                    }
                })
            });
        }));

        $(".cancel-button").on("click",window.CloseOwnerWindow);

        function getData(validate) {
            var fromData = mainForm.getData(validate);
            if (dataId) {
                fromData.id = dataId;
            }
            return fromData;
        }

        function loadData(id) {
            require(["request", "message"], function (request, message) {
                var loading = message.loading("加载中...");
                request.get("plugin-category/" + id, function (response) {
                    loading.hide();
                    if (response.status === 200) {
                        mainForm.setData(response.result);

                    } else {
                        message.showTips("加载数据失败", "danger");
                    }
                });
            });
        }
    });
});


