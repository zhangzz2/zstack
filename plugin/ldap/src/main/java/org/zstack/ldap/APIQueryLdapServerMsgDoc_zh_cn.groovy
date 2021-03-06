package org.zstack.ldap

import org.zstack.header.query.APIQueryMessage

doc {
    title "在这里填写API标题"

    desc "在这里填写API描述"

    rest {
        request {
			url "GET /v1/ldap/servers"

			url "GET /v1/ldap/servers/{uuid}"


            header (OAuth: 'the-session-uuid')

            clz APIQueryLdapServerMsg.class

            desc ""
            
			params APIQueryMessage.class
        }

        response {
            clz APIQueryLdapServerReply.class
        }
    }
}