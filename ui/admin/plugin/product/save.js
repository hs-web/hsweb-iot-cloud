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

        require(["text!save.hf", "pages/form/designer-drag/components-default"], function (config) {
            require(["pages/form/designer-drag/components-biz"], function () {

                mainForm = new FormParser(JSON.parse(config));
                mainForm.render($("#basic-info"));

                if (dataId) {
                    loadData(dataId);
                }

                if (readOnly) {
                    $(".save-button").hide();
                    mainForm.setReadOnly(true);
                }

                var costType = mini.getbyName("costType");
                costType.setValue(1);
                mainForm.getbyName("costRule").hide();
                costType.on("valuechanged",function (e) {
                   var type = parseInt(e.value);

                   if (type===1){
                       mainForm.getbyName("costRule").hide();
                   } else if (type===2){
                       mainForm.getbyName("costRule").show();
                   }
                });

                var costRule = mini.getbyName("costRule");

                costRule.setValue(1);
                var priceComponent=mainForm.getbyName("priceList");
                var priceContainer = priceComponent.container;
                var priceBody =priceContainer.find(".component-body");


                costRule.on("valuechanged", function (e) {
                    var rule = parseInt(e.value);

                    if (rule === 1) {
                        priceBody.html("").append("<input name='priceList' class='mini-textbox' style='width: 100%'>");
                        mini.parse();
                        delete priceComponent.getValue;
                    } else if (rule === 2) {
                        initPrice();
                    }
                });
                function initPrice(){
                    var html =[
                        "<div>\n",
                        "<button class=\"mini-button mini-button-info add-program\">增加方案</button>\n" ,
                        "    <div class=\"program-list\">\n" ,
                        "        <div id='i-0' class=\"program\">\n" ,
                        "            <input class=\"mini-combobox program-count\" name=\"programCount\" style=\"width:150px;\" textField=\"value\" valueField=\"id\"\n" ,
                        "                   value=\"1\" data=\"[{'id':'1','value':'一个月'},{'id':'3','value':'三个月'},{'id':'6','value':'六个月'}]\"/>\n" ,
                        "            <input class=\"mini-textbox program-price\" emptyText=\"请输入\"  name=\"programPrice\"/>\n",
                        "        </div>\n" ,
                        "    </div>\n" ,
                        "</div>"
                    ];

                    priceBody.html("").append(html.join(""));
                    mini.parse();
                    $(".add-program").on("click",function () {
                        var id = "i"+new Date().getTime();

                        var html = '<div class="program" id="'+id+'">\n' +
                            '        <input class="mini-combobox program-count" style="width:150px;" name="programCount" textField="value" valueField="id"\n' +
                            '               value="1" data="[{\'id\':\'1\',\'value\':\'一个月\'},{\'id\':\'3\',\'value\':\'三个月\'},{\'id\':\'6\',\'value\':\'六个月\'}]"/>\n' +
                            '        <input class="mini-textbox program-price" emptyText="请输入"  name="programPrice"/>\n' +
                            '<a class="mini-button" plain="true" iconCls="icon-remove" id="del'+id+'"></a>'
                            '    </div>';

                        $(".program-list").append(html);
                        mini.parse();
                        $("#del"+id).on("click",function () {
                            $("#"+id).remove();
                        });
                    });
                    //设置获取value
                    priceComponent.getValue=function () {
                        var list = [];
                        $(".program").each(function () {
                          var id = $(this).attr("id");
                            list.push(new mini.Form("#"+id).getData());
                        });
                        return list;
                    }
                }


            });
        });



        $(".save-button").on("click", (function () {
            require(["message"], function (message) {
                var data = getData(true);
                console.log(data);
                if (!data) {
                    return;
                }
                if (dataId) {
                    data.id = dataId;
                }
                var loading = message.loading("提交中");

                request.patch('plugin-product', data, function (response) {
                    loading.hide();
                    if (response.status === 200) {
                        dataId = response.result;
                        message.showTips("提交成功");
                    } else if (response.status === 400) {
                        message.showTips(response.message, "danger");
                        mainForm.setErrors(response.result);
                    } else {
                        mini.alert(response.message);
                    }
                })
            });
        }));

        $(".cancel-button").on("click", window.CloseOwnerWindow);

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
                request.get("plugin-product/" + id, function (response) {
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


