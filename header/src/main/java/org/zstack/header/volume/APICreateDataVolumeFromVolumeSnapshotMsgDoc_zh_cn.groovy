package org.zstack.header.volume



doc {
    title "在这里填写API标题"

    desc "在这里填写API描述"

    rest {
        request {
			url "POST /v1/volumes/data/from/volume-snapshots/{volumeSnapshotUuid}"


            header (OAuth: 'the-session-uuid')

            clz APICreateDataVolumeFromVolumeSnapshotMsg.class

            desc ""
            
			params {

				column {
					name "name"
					enclosedIn "params"
					desc "资源名称"
					location "body"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "description"
					enclosedIn "params"
					desc "资源的详细描述"
					location "body"
					type "String"
					optional true
					since "0.6"
					
				}
				column {
					name "volumeSnapshotUuid"
					enclosedIn "params"
					desc "云盘快照UUID"
					location "url"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "primaryStorageUuid"
					enclosedIn "params"
					desc "主存储UUID"
					location "body"
					type "String"
					optional true
					since "0.6"
					
				}
				column {
					name "resourceUuid"
					enclosedIn "params"
					desc ""
					location "body"
					type "String"
					optional true
					since "0.6"
					
				}
				column {
					name "systemTags"
					enclosedIn ""
					desc ""
					location "body"
					type "List"
					optional true
					since "0.6"
					
				}
				column {
					name "userTags"
					enclosedIn ""
					desc ""
					location "body"
					type "List"
					optional true
					since "0.6"
					
				}
			}
        }

        response {
            clz APICreateDataVolumeFromVolumeSnapshotEvent.class
        }
    }
}