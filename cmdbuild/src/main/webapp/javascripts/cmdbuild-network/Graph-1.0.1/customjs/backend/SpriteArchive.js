(function($) {
	var SpriteArchive = {
		class2Sprite: function(classId) {
			var defaultShape = $.Cmdbuild.g3d.constants.DEFAULT_SHAPE;
			var prefix = $.Cmdbuild.appConfigUrl + $.Cmdbuild.g3d.constants.SPRITES_PATH;
			var sprite = "";
			sprite = this.keyInArchive("systemKeys", classId);
			if (sprite) {
				return prefix + sprite;				
			}
			sprite = this.keyInArchive("networkKeys", classId);
			if (sprite) {
				return prefix + sprite;				
			}
			sprite = this.keyInArchive("systemKeys", defaultShape);
			if (sprite) {
				return prefix + sprite;				
			}
			console.log("SpriteArchive not found default sprite key:" + classId);
			return "";
		},
		keyInArchive: function(database, classId) {
			var class2Key = $.Cmdbuild.custom.configuration.class2Key;
			var key2Sprite = $.Cmdbuild.custom.configuration.key2Sprite;
			if (class2Key[database] && class2Key[database][classId] && key2Sprite[class2Key[database][classId]]) {
				return key2Sprite[class2Key[database][classId]];
			}
			else {
				return undefined;
			}
		}
	};
	$.Cmdbuild.SpriteArchive = SpriteArchive;
})(jQuery);