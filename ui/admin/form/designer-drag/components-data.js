(function () {
    //  componentRepo.useIdForName=true;

    function createDefaultEditor() {
        var properties = [
            {
                id: "name",
                comment: "由字母数字或下划线组成",
                text: "字段",
                value: ""
            }, {
                id: "comment",
                editor: "textbox",
                comment: "控件的中文描述",
                text: "描述",
                value: "新建控件"
            }, createTrueOrFalseEditor("showComment", "显示描述", "true"), {
                id: "emptyText",
                text: "提示",
                value: ""
            }, {
                id: "type",
                editor: "textbox",
                text: "控件类型",
                createEditor: function (component, text, value, call) {
                    value = value || component.type;
                    var html = $("<input name='type' allowInput=\"true\" expandOnLoad='true'  style='width: 100%' class='mini-treeselect'>");
                    html.val(value);
                    if (!window.__components) {
                        window.__components = [];
                        var cache = {};
                        $(componentRepo.supportComponentsList)
                            .each(function () {
                                if (!cache[this.type]) {
                                    cache[this.type] = {id: this.type, text: this.type};
                                    cache[this.type].children = [];
                                }
                                var tmp = new this();
                                cache[this.type].children.push({id: tmp.type, text: tmp.getProperty("comment").value});
                            });
                        for (var type in cache) {
                            window.__components.push(cache[type])
                        }
                    }

                    window.onbeforenodeselect_00001 = function (e) {
                        if (e.isLeaf === false) e.cancel = true;
                    };
                    html.attr({
                        "data": " window.__components",
                        "onbeforenodeselect": "onbeforenodeselect_00001"
                    });
                    return html;
                }
            }, {
                id: "width",
                text: "控件宽度",
                value: "4",
                createEditor: function (component, text, value, call) {
                    var html = $("<div style='margin-left: 4px;position: relative;top: 9px;width: 92%'>");
                    html.slider({
                        orientation: "horizontal",
                        range: "min",
                        min: 1,
                        max: 12,
                        value: value,
                        slide: function () {
                            if (call) call()
                            component.setProperty("width", arguments[1].value);
                        }
                    });
                    return html;
                }
            }, {
                id: "size",
                text: "控件宽度",
                value: "4",
                hide: true
            }, {
                id: "height",
                text: "控件高度",
                value: "",
                createEditor: function (component, text, value, call) {
                    var html = $("<div style='margin-left: 4px;position: relative;top: 9px;width: 92%'>");
                    html.slider({
                        orientation: "horizontal",
                        range: "min",
                        min: 30,
                        max: 800,
                        value: value,
                        slide: function () {
                            if (call) call();
                            if (parseInt(arguments[1].value) <= 30) {
                                component.setProperty("height", undefined);
                            } else {
                                component.setProperty("height", arguments[1].value);
                            }
                        }
                    });
                    return html;
                }
            },
            {
                id: "required",
                editor: "radio",
                text: "是否必填",
                value: "undefined",
                createEditor: function (component, text, value) {
                    var checkbox = $("<input class='mini-radiobuttonlist' name='required' value='" + value + "'>");
                    checkbox.attr("data", JSON.stringify([{id: "required", text: "是"}, {
                        id: 'undefined',
                        checked: true,
                        text: "否"
                    }]));
                    return checkbox;
                }
            }
        ];
        if (componentRepo.useIdForName) {
            var name = properties[0];
            name.hide = true;
            name.getValue = function (component) {
                name.value = component.id;
                return component.id;
            }
        }
        return properties;
    }

    function createClass(O, T, name) {
        (function () {
            // 创建一个没有实例方法的类
            var Super = function () {
            };
            Super.prototype = T ? T.prototype : Component.prototype;
            //将实例作为子类的原型
            O.prototype = new Super();
            O.type = name || "基础控件";
        })();
    }

    function createDataSourceEditor() {
        return {
            id: "option",
            text: "选项配置",
            value: {
                type: "data",
                data: [{id: "1", text: '选项1'}, {id: "2", text: '选项2'}]
            },
            createEditor: function (component, text, value) {
                var button = $("<a class='mini-button' plain='true' onclick='edit_datasource_00001' iconCls='icon-edit'>");
                window.edit_datasource_00001 = function () {
                    editOptional(component.getProperty("option").value, component.type, function (config) {
                        component.setProperty("option", mini.clone(config));
                        mini.parse();
                    });
                };
                return button;
            }

        }
    }

    function createScriptEditor(id, text, lang) {
        return {
            id: id,
            text: text,
            script: true,
            value: "",
            createEditor: function (component, text, value) {
                var button = $("<a class='mini-button' plain='true' onclick='window.edit_script_" + id + "' iconCls='icon-edit'>");
                window['edit_script_' + id] = function () {
                    editScript(lang, component.getProperty(id).value || "", null, function (editor) {
                        component.setProperty(id, editor.getScript());
                        mini.parse();
                    });
                };
                return button;
            }

        }
    }

    function createTrueOrFalseEditor(id, text, value) {
        return {
            id: id,
            text: text,
            value: value,
            createEditor: function (component, text, value) {
                var checkbox = $("<input class='mini-radiobuttonlist' name='" + id + "' value='" + value + "'>");
                checkbox.attr("data", JSON.stringify([{id: "true", text: "是"}, {
                    id: 'false',
                    checked: true,
                    text: "否"
                }]));
                return checkbox;
            }
        }
    }

    function createOtherElEditor(options) {
        return {
            id: "elProperties",
            text: "其他配置",
            value: {},
            createEditor: function (component, text, value) {
                var button = $("<a class='mini-button' plain='true' onclick='elProperties_00001' iconCls='icon-edit'>");
                window.elProperties_00001 = function () {

                };
                return button;
            }

        }
    }

    function createOnclick(script) {
        var me = this;
        var eventId = "e_" + (Math.round(Math.random() * 100000));
        var call = typeof script === 'function' ? script : function () {
            if (script) {
                try {
                    var fun = eval("(function(){return function(){\n" +
                        script +
                        "\n}})()");
                    fun.call(me);
                } catch (e) {
                    console.log(script, e);
                }
            }
        };
        window[eventId] = call;
        return eventId;
    };

    /**数据表格**/
    {
        function DataTable(id) {
            Component.call(this);
            this.id = id;
            this.properties = createDefaultEditor();
            // this.removeProperty("name");
            this.removeProperty("required");
            this.removeProperty("placeholder");
            this.getProperty("comment").value = "数据表格";
            this.getProperty("width").value = "12";
            this.getProperty("height").value = "200";
            this.properties.push({
                id: "config",
                text: "表格配置",
                value: {
                    type: "datagrid", //类型: datagrid,treegrid
                    ajaxType: "GET"
                }
            });
            this.properties.push({
                id: "columns",
                text: "列配置",
                value: [
                    {type: "indexcolumn", header: "#", headerAlign: "center", align: "center"},
                    {
                        field: "column1",
                        width: 120,
                        headerAlign: "center",
                        align: "center",
                        allowSort: "false",
                        header: "列1"
                    },
                    {
                        field: "column2",
                        width: 120,
                        headerAlign: "center",
                        align: "center",
                        allowSort: "false",
                        header: "列2"
                    }
                ],
                createEditor: function (component, text, value) {
                    var button = $("<a class='mini-button' plain='true' onclick='edit_column_00001' iconCls='icon-edit'>");
                    window.edit_column_00001 = function () {
                        require(['data-table/edit-columns'], function (ColumnsConfigEditor) {
                            var editor = new ColumnsConfigEditor({
                                onload: function () {
                                    this.setData(value);
                                    this.show();
                                }
                            });

                        })
                    };
                    return button;
                }
            });
            this.properties.push({
                id: "events",
                text: "事件配置",
                value: [
                    {id: "load", text: "当加载完成时触发"}
                ]
            })
        }

        createClass(DataTable, Component, "数据控件");

        DataTable.prototype.setHeight = function (height) {
            this.container.css("height", height ? height + "px" : "");

            mini.parse();
        }
        DataTable.prototype.getGrid = function () {
            var id = this.gridId;
            return mini.get(id);
        }
        DataTable.prototype.reload = function () {
            var me = this;
            var container = me.container;
            var id = me.gridId;
            if (!id) {
                id = me.gridId = "grid-" + me.id;
            }

            function createTable() {
                var dataGrid = $("<div showPager='false' style='width: 100%;height: 100%' class='mini-datagrid'>");
                dataGrid.attr("id", id);
                return dataGrid;
            }

            container.find(".grid-container")
                .html(createTable());
            mini.parse();
            var grid = me.getGrid();
            if (grid) {
                var columns = me.getProperty("columns").value;
                grid.set({
                    columns: columns
                });
            }
        }
        DataTable.prototype.render = function () {
            var me = this;


            var container = this.getContainer(function () {
                var height = me.getProperty("height").value;

                var m = $("<div class='mini-fit mini-col-12 form-component'>").css({
                    "height": height + "px",
                    "overflow-x": "hidden"
                });
                var fit = $("<fieldset style='border: 0;height: 100%;width: 100%' class='child-form'>")
                    .append($("<legend class='edit-focus form-hidden'>数据表格</legend>"));
                var body = $("<div class='mini-fit grid-container'>");

                m.append(fit.append(body));
                return m;
            });

            function reinitTable() {

                me.reload();
            }

            reinitTable();
            this.un("propertiesChanged")
                .on('propertiesChanged', function (key, value) {
                    if (key === 'bodyHeight') {
                        container.find(".table:first").css("height", value);
                    } else {
                        reinitTable();
                    }
                });
            return container;
        };
        DataTable.prototype.typeName = "数据表格";

        DataTable.icon = "iconfont icon-biaoge";
        componentRepo.registerComponent("datatable", DataTable);

    }
    /**搜索框**/
    {
        function SearchBox(id) {
            Component.call(this);
            this.id = id;
            this.properties = createDefaultEditor();
            // this.removeProperty("name");
            this.removeProperty("required");
            this.removeProperty("placeholder");
            this.getProperty("comment").value = "搜索条件";
            this.getProperty("width").value = "12";
            this.getProperty("height").value = "200";
            this.properties.push({
                id: "targetGrid",
                text: "目标表格",
                value: {},
                createEditor: function (component, text, value) {
                    var button = $("<input class='mini-combobox' name='targetGrid'/>");

                    var value = [];
                    for (var i in designer.components) {
                        if (designer.components[i].type === 'datatable') {
                            value.push({
                                id: designer.components[i].id,
                                text: designer.components[i].getProperty('comment').value
                            })
                        }
                    }
                    button.attr("data", JSON.stringify(value));
                    return button;
                }
            });
        }

        createClass(SearchBox, Component, "数据控件");

        SearchBox.prototype.setHeight = function (height) {
            this.container.css("height", height ? height + "px" : "");
        };
        SearchBox.prototype.getGrid = function () {
            var id = this.getProperty("targetGrid").value;
            return mini.get("grid-" + id);
        }
        SearchBox.prototype.beforeSearch = function (formData) {
            return formData;
        }
        SearchBox.prototype.doSearch = function () {
            var formData = this.getFormData();
            var grid = this.getGrid();
            var me = this;
            if (grid) {
                require(['request'], function (request) {
                    var queryParam = request.encodeQueryParam(me.beforeSearch(formData));
                    grid.load(queryParam);
                });
            }
        };
        SearchBox.prototype.doReset = function () {
            var form = new mini.Form("#" + this.formId);
            form.reset();
            this.doSearch();
        }
        SearchBox.prototype.getFormData = function () {
            var me = this;
            var form = new mini.Form("#" + this.formId);

            var data = form.getData();
            var condition = this.container.find(".search-container");
            condition.find(".form-component").each(function () {
                var componentId = $(this).attr("hs-id");
                if (componentId && me.parser && me.parser.get) {
                    var component = me.parser.get(componentId);
                    if (component && component.getData) {
                        data[component.getProperty('name').value] = component.getData(data);
                    }
                }
            });
            return data;
        };

        SearchBox.prototype.reload = function () {
            var container = this.container;
            var meCom = this;
            var condition = container.find(".search-container");
            var formId = meCom.formId = condition.attr("id");
            if (!formId) {
                condition.attr("id", meCom.formId = formId = ("search-" + meCom.id));
            }
            condition.find(".buttons:first").remove();
            var searchButton = $("<a class='mini-button search-button' >")
                .text("查询").attr("onclick", createOnclick(function () {
                    meCom.doSearch();
                }));
            var resetButton = $("<a class='mini-button reset-button' plain='true' >").text("重置")
                .attr("onclick", createOnclick(function () {
                    meCom.doReset();
                }));
            var expandButton = $("<span style='cursor: pointer;font-size: 14px;color: #1890ff' >");
            var line1len = 0;
            condition.find(".form-component:lt(2)")
                .each(function () {
                    $($(this).attr("class").split(" ")).each(function () {
                        if (this.indexOf("mini-col-") === 0) {
                            line1len += parseInt(this.substr(9));
                        }
                    })
                });

            expandButton.append(
                $("<span class='text'>").text("展开"))
                .append($("<i style='margin-left: 0.2em' class='fa fa-angle-down'>"))
                .on('click', function () {
                    var container = meCom.container;
                    var condition = container.find(".search-container");
                    var me = $(this);
                    var len = condition.find(".form-component").length;
                    var allLen = 0;
                    condition.find(".form-component")
                        .each(function () {
                            $($(this).attr("class").split(" ")).each(function () {
                                if (this.indexOf("mini-col-") === 0) {
                                    allLen += parseInt(this.substr(9));
                                }
                            })
                        });

                    var size = (12 - (allLen % 12));
                    var text = me.find('.text');
                    var icon = me.find(".fa");
                    if (text.text() === '展开') {
                        text.text("收起");
                        icon.removeClass("fa-angle-down").addClass("fa fa-angle-up");
                        condition.find(".form-component:gt(1)").show();
                        condition.find(".buttons")
                            .removeClass()
                            .addClass("buttons")
                            .addClass("mini-col-" + size)
                            .css("text-align", "right");
                        mini.parse();
                    } else {
                        if (text.text() === '收起') {
                            text.text("展开");
                            icon.removeClass("fa-angle-up")
                                .addClass("fa fa-angle-down");
                            condition.find(".form-component:gt(1)").hide();
                            condition.find(".buttons").removeClass()
                                .addClass("buttons")
                                .addClass("mini-col-" + (12 - (line1len % 12)))
                                .css("text-align", "left");
                            mini.parse();
                        }
                    }
                });

            var conditionLen = condition.children().length;
            if (conditionLen < 3) {
                expandButton.hide();
            }
            if (conditionLen > 0) {
                condition.append($("<div class='buttons mini-col-" + (12 - (line1len % 12)) + "' style='text-align: left'>")
                    .append(searchButton)
                    .append("<span style='margin-left: 0.5em'>")
                    .append(resetButton)
                    .append("<span style='margin-left: 0.5em'>")
                    .append(expandButton));
            }
            mini.parse();
            condition.find(".form-component:gt(1)").hide();
        }
        SearchBox.prototype.render = function () {
            var me = this;
            var container = this.getContainer(function () {
                var height = me.getProperty("height").value;

                var m = $("<div class='mini-col-12 form-component'>").css({
                    "height": height + "px",
                    "overflow-x": "hidden"
                });
                var fit = $("<fieldset style='border: 0 solid grey;height: 100%;width: 100%' class='child-form search-box'>")
                    .append($("<legend class='edit-focus form-hidden'>搜索条件</legend>"));
                var body = $("<div style='height: 100%' class='search-container components'>");

                m.append(fit.append(body));
                return m;
            });

            function reinitTable() {
                me.reload();
            }

            reinitTable();
            this.un("propertiesChanged")
                .on('propertiesChanged', function (key, value) {
                    if (key === 'bodyHeight') {
                        container.find(".table:first").css("height", value);
                    } else {
                        reinitTable();
                    }
                });
            return container;
        };
        SearchBox.prototype.typeName = "搜索条件";

        SearchBox.icon = "iconfont icon-danhangshurukuang";
        componentRepo.registerComponent("searchbox", SearchBox);
    }


})();