importResource("/admin/css/common.css");

require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        require(["miniui-tools"], function (tools) {
            mini.parse();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setDataField("result");
            grid.setUrl(API_BASE_PATH + "district/no-paging");
            $(".search-button").on("click", function () {
                var keyword = mini.getbyName("keyword").getValue();
                var param = {};
                if (keyword && keyword.length > 0) {
                    require(["request"], function (request) {
                        param = request.createQuery().where()
                            .like("name", "%" + keyword + "%")
                            .or().like("fullName", "%" + keyword + "%")
                            .or().like("code", "%" + keyword + "%")
                            .getParams();
                        grid.load(param);
                    });
                } else {
                    grid.load(param);
                }
            });
            //加载行政区划等级 TODO

            $(".save-all-button").on("click", function () {
                require(["request", "message"], function (request, message) {
                    message.confirm("确认保存全部行政区划数据", function () {
                        grid.loading("保存中...");
                        request.patch("district/batch", grid.getData(), function (response) {
                            if (response.status === 200) {
                                message.showTips("保存成功");
                                grid.reload();
                            } else {
                                message.showTips("保存失败:" + response.message);
                            }
                        });
                    });
                })
            });
            var newDataParentId;
            window.renderAction = function (e) {
                var html = [];
                var row = e.record;
                if (authorize.hasPermission("district", "add")) {
                    html.push(tools.createActionButton("添加子级行政区划", "icon-add", function () {
                        var sortIndex = row.sortIndex ? (row.sortIndex + "0" + (row.chidren ? row.chidren.length + 1 : 1)) : 1;
                        grid.addNode({sortIndex: sortIndex}, row.chidren ? row.chidren.length : 0, row);
                        newDataParentId = row.id;
                    }));
                }
                if (row._state === "added" || row._state === "modified") {
                    html.push(tools.createActionButton("保存", "icon-save", function () {
                        var api = "district/";
                        require(["request", "message"], function (request, message) {
                            var loading = message.loading("保存中...");
                            request.patch(api, row, function (res) {
                                loading.hide();
                                if (res.status == 200) {
                                    request.get(api + res.result, function (data) {
                                        grid.updateNode(row, data.result);
                                        grid.acceptRecord(row);
                                        message.showTips("保存成功!");
                                    });
                                } else {
                                    message.showTips("保存失败:" + res.message, "danger");
                                }
                            })
                        });
                    }));
                }
                if (authorize.hasPermission("district", "update")) {
                    html.push(tools.createActionButton("编辑网格信息", "icon-edit", function () {
                        editGrid(row);
                    }));
                }
                return html.join("");
            }

            function editGrid(row) {
                var url;
                console.log(row);
                if (row.id) {
                    url = "admin/npx-form/grid-manager/grid-manager.html?id=" + row.id + "&parentId=" + row.parentId;
                } else {
                    url = "admin/npx-form/grid-manager/grid-manager.html?parentId=" + newDataParentId;
                }
                tools.openWindow(url, "编辑网格", "800", "600", function () {
                    grid.reload();
                });
            }

            function addGrid(id) {
                tools.openWindow("admin/npx-form/grid-manager/grid-manager.html?parentId=" + id, "添加网格", "800", "600", function () {
                    grid.reload();
                });
            }
        });
    });
});
