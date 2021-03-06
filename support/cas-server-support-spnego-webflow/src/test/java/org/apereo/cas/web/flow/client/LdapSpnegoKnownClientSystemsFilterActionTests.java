package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = "classpath:/spnego-ldap-context.xml")
public class LdapSpnegoKnownClientSystemsFilterActionTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("provisioningConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    private SearchRequest searchRequest;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void ensureLdapAttributeShouldDoSpnego() {
        final LdapSpnegoKnownClientSystemsFilterAction action = new LdapSpnegoKnownClientSystemsFilterAction("", "", 0, this.connectionFactory,
                this.searchRequest, "mail") {
            @Override
            protected String getRemoteHostName(final String remoteIp) {
                if ("localhost".equalsIgnoreCase(remoteIp) || remoteIp.startsWith("127")) {
                    return remoteIp;
                }
                return super.getRemoteHostName(remoteIp);
            }
        };
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("localhost");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }
}
