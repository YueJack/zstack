package org.zstack.header.volume

org.zstack.header.volume.APIDetachDataVolumeFromVmEvent

doc {
    title "DetachDataVolumeFromVm"

    category "volume"

    desc "在这里填写API描述"

    rest {
        request {
			url "DELETE /v1/volumes/{uuid}/vm-instances"


            header (OAuth: 'the-session-uuid')

            clz APIDetachDataVolumeFromVmMsg.class

            desc ""
            
			params {

				column {
					name "uuid"
					enclosedIn ""
					desc "资源的UUID，唯一标示该资源"
					location "url"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "vmUuid"
					enclosedIn ""
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
            clz APIDetachDataVolumeFromVmEvent.class
        }
    }
}