importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools", "message", "search-box"], function (request, tools, message,SearchBox) {

        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize:2
        }).init();

        var grid = window.grid = mini.get("datagrid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath + "plugin");

        var params = request.createQuery().orderByDesc("createTime").getParams();
        grid.load(params);
        function search() {
            tools.searchGrid("#search-box", grid);

        }

        var categoryCombox = mini.get("category");

        var categoryMap = {}
        request.get("plugin-category", function (e) {
            var data = e.result.data;
            categoryCombox.setData(data);
            $(data).each(function () {
                categoryMap[this.id] = this;
            });
            window.renderCategory = function (e) {

                return categoryMap[e.value] ? categoryMap[e.value].name : "";
            }
        });


        $(".search-button").click(search);
        tools.bindOnEnter("#search-box", search);

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/save.html", "添加插件", "1200", "800", function () {
                grid.reload();
            })
        });

        function edit(id) {
            tools.openWindow("admin/plugin/save.html?id=" + id, "编辑插件", "800", "600", function () {
                grid.reload();
            })
        }

        function editVersion(id) {
            tools.openWindow("admin/plugin/version/save.html?id=" + id, "编辑插件版本", "800", "600", function () {
                versionGrid.reload();
            })
        }


        /**
         * 修改插件状态
         * @param id
         * @param status
         */
        function updatePluginStatus(id, status) {
            request.post("plugin/update-status", {
                pluginId: id,
                status: status
            }, function (e) {
                if (e.status === 200) {
                    grid.reload();
                }
            })

        }

        /**
         * 修改插件版本状态
         * @param id
         * @param status
         */
        function updateVersionStatus(id, status) {
            request.post("plugin-version/update-status", {
                pluginId: id,
                status: status
            }, function (e) {
                if (e.status === 200) {
                    versionGrid.reload();
                    // onShowRowDetail();
                } else {
                    message.alert(e.message);
                }
            })

        }

        window.renderAction = function (e) {
            var row = e.record;

            var html = [];
            if (authorize.hasPermission("plugin", "get")) {
                html.push(tools.createActionButton("详情", "icon-find", function () {
                    var versionForm = mini.get("info-form");
                    versionForm.showAtPos('center', 'middle');

                    var content = $("#content");

                    var installType = {1:{name:"预置安装"},2:{name:"自主安装"}};
                    var dictMapping = {
                        "categoryId": categoryMap,
                        "preset": installType
                    }

                    request.createQuery("plugin/" + row.id).orderByDesc("createTime").exec(function (e) {
                    // request.get("plugin/" + row.id, function (e) {
                        if (e.status===200){
                            var data = e.result;
                            request.get("/user-server/district/"+data.area,function(e){
                                if (e.status===200){
                                    data.areaName = e.result.fullName;
                                    for (var key in data) {
                                        var value = data[key];
                                        console.log(value);
                                        if (dictMapping[key]) {
                                            value = dictMapping[key][value] ? dictMapping[key][value].name : value;
                                        }
                                        if (typeof (value) === 'object'){
                                            content.find("." + key).text(value.text || "");
                                        }else {
                                            content.find("." + key).text(value || "");
                                        }
                                    }
                                } else {
                                    message.alert("数据加载失败");
                                }

                            });
                        } else {
                            message.alert("数据加载失败");
                        }

                    });

                }));

            }
            if (authorize.hasPermission("plugin", "edit")) {
                html.push(tools.createActionButton("编辑", "icon-edit", function () {
                    edit(row.id);
                }));
            }

            if (authorize.hasPermission("plugin-version", "add")) {
                html.push(tools.createActionButton("新增版本", "icon-add", function () {
                    tools.openWindow("/admin/plugin/version/save.html", "发布新版", "1200", "700", function () {
                        versionGrid.reload();
                    }, function () {

                        var iframe = this.getIFrameEl();
                        var win = iframe.contentWindow;

                        function init() {
                            win.onInit = function (e) {
                                e.setData({pluginId: row.id, author: row.author})
                            }
                        }

                        init();
                        $(iframe).on("load", init);
                    })
                }));
            }

            if (row.status.value === "RELEASE") {
                if (authorize.hasPermission("plugin","update")){
                    html.push(tools.createActionButton("下架", "icon-download", function () {
                        updatePluginStatus(row.id, "DISCARD");

                    }));
                }
            }
            if (row.status.value === "DISCARD") {
                if (authorize.hasPermission("plugin","update")){
                    html.push(
                        tools.createActionButton("上架", "icon-upload", function () {
                            updatePluginStatus(row.id, "RELEASE");
                        }));
                }
            }
            if (row.status.value === "PRIVATE") {

                if (authorize.hasPermission("plugin","delete")){
                    html.push(tools.createActionButton("删除插件", "icon-remove", function () {
                        require(["message"], function (message) {
                            message.confirm("确定删除该插件？", function () {
                                var loading = message.loading("删除中...");
                                request["delete"]("plugin/" + row.id, {}, function (res) {
                                    loading.close();
                                    if (res.status === 200) {
                                        grid.reload();
                                    } else {
                                        message.showTips("删除失败" + res.message);
                                    }
                                })
                            })
                        });

                    }));
                }
                if (authorize.hasPermission("plugin","update")){
                    html.push(tools.createActionButton("上架", "icon-upload", function () {
                        updatePluginStatus(row.id, "RELEASE");
                    }));
                    html.push(tools.createActionButton("编辑", "icon-edit", function () {
                        edit(row.id);
                    }));
                }
            }

            return html.join("");
        }

        window.renderIcon = function (e) {
            var row = e.record;
            var html = '<img style="width: 20px;height: 20px" src="' + row.icon + '">';
            return html;
        }

        var versionGrid = mini.get("version-datagrid");
        var nowSelectPlugin;

        var versionForm = document.getElementById("version-form");
        window.onShowRowDetail = function (e) {
            var grid = e.sender;
            var row = nowSelectPlugin = e.record;

            tools.initGrid(versionGrid);
            versionGrid.setUrl(request.basePath + "plugin-version/");
            var params = request.createQuery().where("pluginId", row.id).orderByDesc("createTime").getParams();
            versionGrid.load(params);

            var td = grid.getRowDetailCellEl(row);
            td.appendChild(versionForm);
            versionForm.style.display = "block";

        }

        window.renderStatus = function (e) {
            var name = '';
            if (e.value === 'RELEASE') {
                name = '已上架';
            } else if (e.value === 'PRIVATE') {
                name = '未上架';
            } else if (e.value === 'DISCARD') {
                name = '已下架';
            } else {
                name = '其他';
            }
            return name;
        }

        window.renderPreset = function (e) {
            var name = '';
            if (e.value === 1) {
                name = "预置安装";
            } else if (e.value === 2) {
                name = "自主安装";
            }
            return name;
        }

        window.renderUrl = function (e) {
            var name = "";
            if (e.value != null || e.value !== '') {
                name = '<a href="' + e.value + '">点击下载</a>';
            }
            return name;
        }

        window.renderVersionAction = function (e) {
            var row = e.record;
            var html = [];
            var status = row.status.value;
            //nowSelectPlugin.preset === 1 && 判断是否预置插件
            if (status === "RELEASE") {
                if (authorize.hasPermission("plugin-version", "handle")) {
                    html.push(tools.createActionButton("静默操作", "icon-goto", function () {
                        tools.openWindow("admin/plugin/version/gateway/list.html", "网关列表", "1200", "600", null, function () {
                            var iframe = this.getIFrameEl();
                            var win = iframe.contentWindow;

                            function init() {
                                win.onInit = row;
                            }

                            init();
                            $(iframe).on("load", init);
                        })
                    }));
                }
            }
            if (status === "RELEASE") {
                if (authorize.hasPermission("plugin-version", "update-status")) {
                    html.push(tools.createActionButton("下架", "icon-download", function () {
                        updateVersionStatus(row.id, "DISCARD");
                    }));
                }
            }
            if (status === "DISCARD") {
                if (authorize.hasPermission("plugin-version", "update-status")) {
                    html.push(
                        tools.createActionButton("上架", "icon-upload", function () {
                            updateVersionStatus(row.id, "RELEASE");
                        }));
                }
            }

            if (status === "PRIVATE") {
                if (authorize.hasPermission("plugin-version", "update-status")) {
                    html.push(
                        tools.createActionButton("上架", "icon-upload", function () {
                            updateVersionStatus(row.id, "RELEASE");
                        }));
                }
                if (authorize.hasPermission("plugin-version","update")){
                    html.push(tools.createActionButton("编辑", "icon-edit", function () {
                        editVersion(row.id);
                    }));
                }
                if (authorize.hasPermission("plugin-version","delete")){
                    html.push(tools.createActionButton("删除版本", "icon-remove", function () {
                        require(["message"], function (message) {
                            message.confirm("确定删除该版本？", function () {
                                var loading = message.loading("删除中...");
                                request["delete"]("plugin-version/" + row.id, {}, function (res) {
                                    loading.close();
                                    if (res.status === 200) {
                                        versionGrid.reload();
                                    } else {
                                        message.showTips("删除失败" + res.message);
                                    }
                                })
                            })
                        })
                    }));
                }
            }
            return html.join("");
        }

        //提示
        var tip = new mini.ToolTip();
        tip.set({
            target: document,
            selector: '[title]'
        });
    });
});