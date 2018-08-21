importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools", "message", "search-box"], function (request, tools, message,SearchBox) {


        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize:2
        }).init();
        var pluginVersionId = request.getParameter("pluginVersionId");
        var grid = window.grid = mini.get("datagrid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath + "query-server/plugin-gateway/info");


        function search() {
            //statusCombol
            var statusCombol = mini.get('statusCombol').getValue();
            var sn = mini.getbyName("sn").getValue();
            var query = request.createQuery()
                .where("deviceGateway.serialNo", sn);

            if (statusCombol === 'yes') {
                query.notnull("pluginGateway.id")
            }
            if (statusCombol === 'no') {
                query.isnull("pluginGateway.id")
            }
            var param = query.getParams();
            param.param = {pluginId: pluginVersionId};
            grid.load(param)

        }

        search();

        window.renderStatus = function (e) {
            var name = '';
            if (e.value == 'UNVERIFIED') {
                name = '未激活';
            } else if (e.value == 'SUCCESS') {
                name = '激活成功';
            } else if (e.value == 'FORBIDDEN') {
                name = '禁用';
            } else {
                name = '其他';
            }
            return name;
        }

        window.pluginStatus = function (e) {
            var name = '';
            if (e.value == 'INSTALL') {
                name = '安装中';
            } else if (e.value == 'UPDATE') {
                name = '更新中';
            } else if (e.value == 'UNINSTALL') {
                name = '卸载中';
            } else if (e.value == 'START') {
                name = '开始中';
            } else if (e.value == 'STOP') {
                name = '停用中';
            }else if (e.value == 'INSTALL_SUCCESS') {
                name = '安装成功';
            } else if (e.value == 'UPDATE_SUCCESS') {
                name = '更新成功';
            }else if (e.value == 'UNINSTALL_SUCCESS') {
                name = '卸载成功';
            } else if (e.value == 'START_SUCCESS') {
                name = '开始成功';
            }else if (e.value == 'STOP_SUCCESS') {
                name = '停用成功';
            } else if (e.value == 'INSTALL_ERROR') {
                name = '安装失败';
            }else if (e.value == 'UPDATE_ERROR') {
                name = '更新失败';
            } else if (e.value == 'UNINSTALL_ERROR') {
                name = '卸载失败';
            }else if (e.value == 'START_ERROR') {
                name = '开始失败';
            } else if (e.value == 'STOP_ERROR') {
                name = '停用失败';
            }else {
                name = '其他';
            }
            return name;
        }

        var statusCombol = mini.get("statusCombol");
        statusCombol.on("valuechanged", function () {

        });

        $(".search-button").click(search);
        tools.bindOnEnter("#search-box", search);

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/version/save.html", "添加插件版本", "800", "600", function () {
                grid.reload();
            })
        });

        $(".install-button").click(function () {
            sendCommand("install")
        });

        $(".update-button").click(function () {
            sendCommand("update")
        });

        $(".uninstall-button").click(function () {
            sendCommand("uninstall");
        });

        $(".start-button").click(function () {
            sendCommand("start");
        });

        $(".stop-button").click(function () {
            sendCommand("stop");
        });


        //发送指令
        function sendCommand(command) {
            debugger;
            var pluginVersion = window.onInit;
            var rows = grid.getSelecteds();
            if (rows.length > 0) {
                var loding = message.loading("正在发送安装指令...");
                var list = [];
                for (var i = 0; i < rows.length; i++) {
                    var data = {
                        serialNo: rows[i].deviceGateway.serialNo,
                        gatewayId: rows[i].deviceGateway.id,
                        pluginVersionId: pluginVersion.id,
                        pluginId:pluginVersion.pluginId
                    };
                    list.push(data);
                }
                request.post("plugin-gateway/" + command, list, function (e) {

                    loding.hide();
                    if (e.status === 200) {
                        grid.reload();
                    } else {
                        mini.alert(e.message);
                    }
                });
            } else {
                mini.alert("未选中数据");
            }
        }

        function info(id) {
            tools.openWindow("admin/device/device-gateway/save.html?id=" + id + "&readOnly=" + true, "查看设备", "800", "600", function () {
                grid.reload();
            });
        }

        function edit(id) {
            tools.openWindow("admin/device/device-gateway/save.html?id=" + id, "编辑设备", "800", "600", function () {
                grid.reload();
            })
        }
    });
});