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

    /**行政区域选择**/
    {
        function DistrictSelector(id) {
            Component.call(this);
            this.id = id;
            this.properties = createDefaultEditor();
            this.getProperty("width").value = 6;
            this.getProperty("comment").value = "行政区划";
            // this.removeProperty("height");
            this.properties.push(createTrueOrFalseEditor("multiSelect", "是否多选", "false"));
            this.cls = "mini-lookup";

            // this.formText = true;
        }

        createClass(DistrictSelector, TextBox, "高级控件");
        DistrictSelector.prototype.setValue = function (value, data) {
            var me = this;
            require(['request'], function (request) {
                var lookup = mini.getbyName(me.getProperty('name').value);
                request.createQuery('district')
                    .where()
                    .in("id", value).noPaging()
                    .exec(function (resp) {
                        if (resp.status === 200 && resp.result.data) {
                            lookup.setValue(value);
                            var list = [];
                            $(resp.result.data).each(function () {
                                list.push(this.fullName);
                            });
                            lookup.setText(list.join(","));
                        }
                    });
            });
        }
        DistrictSelector.prototype.reload = function () {
            var container = this.container;
            var me = this;
            var id = this.inputId = Math.round(Math.random() * 1000000);

            var panelId = this.panelId = "panel_" + id;
            var gridId = this.gridId = "grid_" + id;

            function createGrid() {
                var query = createOnclick(function () {
                    var grid = mini.get(gridId);
                    var kw = mini.get("dist_kw_" + id).getValue();
                    if (!kw) {
                        grid.reload();
                    } else {
                        require(['request'], function (request) {
                            var param = request.createQuery()
                                .where()
                                .like("fullName", "%" + kw + "%")
                                .or()
                                .like("code", "%" + kw + "%").getParams();
                            grid.load(param);
                        })
                    }
                });

                var expand = createOnclick(function (e) {
                    // console.log(e.params);
                });

                return ["<div id=\"" + panelId + "\" class=\"mini-panel\" title=\"header\" iconCls=\"icon-add\" style=\"width:450px;height:350px;\""
                    , "showToolbar=\"true\"  showCloseButton=\"true\" showHeader=\"false\" bodyStyle=\"padding:0\" borderStyle=\"border:0\""
                    , ">"
                    , "<div property=\"toolbar\" style=\"padding: 5px 5px 5px 8px;text-align:center;\"> "
                    , "<div style=\"float:left;padding-bottom:2px;\">"
                    , "<span>关键字：</span>"
                    , "<input id=\"dist_kw_" + id + "\" class=\"mini-textbox\" style=\"width:160px;\" onenter=\"" + query + "\"/>"
                    , "<a class=\"mini-button\" onclick=\"" + query + "\">查询</a>"
                    , "<a class=\"mini-button\" plain='true' onclick=\"" +
                    createOnclick(function () {
                        mini.get("dist_kw_" + id).setValue("");
                        window[query]();
                        var lookup = mini.getbyName(me.getProperty('name').value);
                        if (lookup) {
                            lookup.deselectAll();
                        }
                    }) + "\">重置</a>"
                    , "</div>"
                    , "<div style=\"float:right;padding-bottom:2px;\">"
                    , "<a class=\"mini-button\" onclick=\"" +
                    createOnclick(function () {
                        var lookup = mini.getbyName(me.getProperty('name').value);
                        if (lookup) {
                            lookup.hidePopup();
                        }
                    })
                    + "\">关闭</a>"
                    , "</div>"
                    , "<div style=\"clear:both;\"></div>"
                    , "</div>"
                    , "<div id=\"" + gridId + "\" class=\"mini-treegrid\" style=\"width:100%;height:100%;\" "
                    , "treeColumn='name' showTreeIcon=\"true\" resultTree='false' onbeforeload='" + expand + "'"
                    , "pageSize='100' resultAsTree='false' idField='id' parentField='parentId' "
                    , "borderStyle=\"border:0\" showPageSize=\"false\" "
                    , "sortField='sortIndex' showPager='false' >"
                    , "<div property=\"columns\">"
                    , "<div type=\"checkcolumn\"></div>"
                    , "<div name='name' field=\"name\" width=\"80\" headerAlign=\"center\" allowSort=\"true\">区划名称</div> "
                    // , "                <div field=\"fullName\" width=\"120\" headerAlign=\"center\" allowSort=\"true\">全称</div>"
                    , "<div field=\"code\" width=\"50\" headerAlign=\"center\" allowSort=\"true\">区划编码</div>"
                    , "</div>"
                    , "</div>"
                    , "</div>"].join("\n")

            }

            function newInput() {
                var input = me.createInput();
                input.attr({
                    "grid": "#" + gridId,
                    "popupWidth": "auto",
                    "popup": "#" + panelId,
                    "textField": "fullName",
                    "valueField": "id"
                });
                return container.find(".component-body")
                    .html("")
                    .append(input)
                    .append($(createGrid()));
            }

            newInput();
            mini.parse();
            require(['request', 'miniui-tools'], function (request, tools) {
                var grid = mini.get(gridId);
                tools.initGrid(grid);
                grid.setDataField("result");
                grid.setAutoLoad(false);
                grid.setUrl(API_BASE_PATH + "district/no-paging");
                var lookup = mini.getbyName(me.getProperty('name').value);
                if (lookup) {
                    var loaded = false;
                    lookup.on('beforeshowpopup', function () {
                        if (!loaded) {
                            grid.load();
                        }
                        loaded = true;
                    })
                }
            })
        };

        DistrictSelector.prototype.setHeight = function (height) {
            if (!height || height <= 1) {
                height = "";
            }
            this.setProperty("areaHeight", height);
            mini.parse();
        };

        DistrictSelector.icon = "iconfont icon-duohangshurukuang";
        DistrictSelector.prototype.typeName = "多行文本";
        componentRepo.registerComponent("district", DistrictSelector);
    }


})();