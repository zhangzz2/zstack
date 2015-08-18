package org.zstack.test.lb;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.header.identity.SessionInventory;
import org.zstack.header.network.l3.L3NetworkInventory;
import org.zstack.header.vm.VmInstanceInventory;
import org.zstack.header.vm.VmNicInventory;
import org.zstack.network.service.lb.LoadBalancerInventory;
import org.zstack.network.service.lb.LoadBalancerListenerVO;
import org.zstack.network.service.lb.LoadBalancerVO;
import org.zstack.network.service.vip.VipVO;
import org.zstack.network.service.virtualrouter.VirtualRouterVmVO;
import org.zstack.network.service.virtualrouter.lb.VirtualRouterLoadBalancerBackend.LbTO;
import org.zstack.network.service.virtualrouter.lb.VirtualRouterLoadBalancerBackend.RefreshLbCmd;
import org.zstack.simulator.appliancevm.ApplianceVmSimulatorConfig;
import org.zstack.simulator.virtualrouter.VirtualRouterSimulatorConfig;
import org.zstack.test.Api;
import org.zstack.test.ApiSenderException;
import org.zstack.test.DBUtil;
import org.zstack.test.WebBeanConstructor;
import org.zstack.test.deployer.Deployer;

/**
 * 
 * @author frank
 * 
 * 1. create a lb
 * 2. stop the vm
 * 3. delete the vr
 * 4. start the vm
 *
 * confirm lb synced on the vr
 *
 * 5. stop the vr
 * 6. start the vr
 *
 * confirm lb synced on the vr
 *
 * 7. reboot the vr
 *
 * confirm lb synced on the vr
 */
public class TestVirtualRouterLb14 {
    Deployer deployer;
    Api api;
    ComponentLoader loader;
    CloudBus bus;
    DatabaseFacade dbf;
    SessionInventory session;
    VirtualRouterSimulatorConfig vconfig;
    ApplianceVmSimulatorConfig aconfig;

    @Before
    public void setUp() throws Exception {
        DBUtil.reDeployDB();
        WebBeanConstructor con = new WebBeanConstructor();
        deployer = new Deployer("deployerXml/lb/TestVirtualRouterLb.xml", con);
        deployer.addSpringConfig("VirtualRouter.xml");
        deployer.addSpringConfig("VirtualRouterSimulator.xml");
        deployer.addSpringConfig("KVMRelated.xml");
        deployer.addSpringConfig("vip.xml");
        deployer.addSpringConfig("lb.xml");
        deployer.build();
        api = deployer.getApi();
        loader = deployer.getComponentLoader();
        vconfig = loader.getComponent(VirtualRouterSimulatorConfig.class);
        aconfig = loader.getComponent(ApplianceVmSimulatorConfig.class);
        bus = loader.getComponent(CloudBus.class);
        dbf = loader.getComponent(DatabaseFacade.class);
        session = api.loginAsAdmin();
    }

    void validate() {
        VmInstanceInventory vm = deployer.vms.get("TestVm");
        LoadBalancerInventory lb = deployer.loadBalancers.get("lb");
        LoadBalancerVO lbvo = dbf.findByUuid(lb.getUuid(), LoadBalancerVO.class);
        Assert.assertNotNull(lbvo);
        Assert.assertNotNull(lbvo.getProviderType());
        Assert.assertFalse(lbvo.getListeners().isEmpty());
        Assert.assertFalse(lbvo.getVmNicRefs().isEmpty());

        VipVO vip = dbf.findByUuid(lbvo.getVipUuid(), VipVO.class);
        Assert.assertNotNull(vip);
        Assert.assertFalse(vconfig.vips.isEmpty());

        Assert.assertFalse(vconfig.refreshLbCmds.isEmpty());
        RefreshLbCmd cmd = vconfig.refreshLbCmds.get(0);
        Assert.assertFalse(cmd.getLbs().isEmpty());
        LbTO to = cmd.getLbs().get(0);
        LoadBalancerListenerVO l = lbvo.getListeners().iterator().next();
        Assert.assertEquals(l.getProtocol(), to.getMode());
        Assert.assertEquals(l.getInstancePort(), to.getInstancePort());
        Assert.assertEquals(l.getLoadBalancerPort(), to.getLoadBalancerPort());

        Assert.assertEquals(vip.getIp(), to.getVip());

        L3NetworkInventory gnw = deployer.l3Networks.get("GuestNetwork");
        VmNicInventory nic = vm.findNic(gnw.getUuid());
        Assert.assertFalse(to.getNicIps().isEmpty());
        String nicIp = to.getNicIps().get(0);
        Assert.assertEquals(nic.getIp(), nicIp);
    }
    
    @Test
    public void test() throws ApiSenderException {
        VmInstanceInventory vm = deployer.vms.get("TestVm");
        VirtualRouterVmVO vr = dbf.listAll(VirtualRouterVmVO.class).get(0);

        api.stopVmInstance(vm.getUuid());
        api.destroyVmInstance(vr.getUuid());
        vconfig.refreshLbCmds.clear();
        api.startVmInstance(vm.getUuid());
        validate();

        vr = dbf.listAll(VirtualRouterVmVO.class).get(0);
        api.stopVmInstance(vr.getUuid());
        vconfig.refreshLbCmds.clear();
        api.startVmInstance(vr.getUuid());
        validate();

        vconfig.refreshLbCmds.clear();
        api.rebootVmInstance(vr.getUuid());
        validate();
    }
}