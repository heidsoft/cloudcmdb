(function () {

	Ext.define("CMDBuild.model.widget.manageRelation.CMRelationPanelModel", {
		extend: "Ext.data.Model",

		fields: [
			'dom_id', 'dom_desc', 'label',
			'dst_code', 'dst_id', 'dst_desc', 'dst_cid',
			'rel_attr', 'rel_date', 'rel_id',
			'relations_size', 'src'
		]
	});

})();
