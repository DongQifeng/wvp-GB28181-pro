package com.genersoft.iot.vmp.vmanager.server;

import com.genersoft.iot.vmp.VManageBootstrap;
import com.genersoft.iot.vmp.media.zlm.dto.MediaServerItem;
import com.genersoft.iot.vmp.service.IMediaServerService;
import com.genersoft.iot.vmp.utils.SpringBeanFactory;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import gov.nist.javax.sip.SipStackImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("rawtypes")
@Api(tags = "服务控制")
@CrossOrigin
@RestController
@RequestMapping("/api/server")
public class ServerController {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private IMediaServerService mediaServerService;


    @ApiOperation("流媒体服务列表")
    @GetMapping(value = "/media_server/list")
    @ResponseBody
    public WVPResult<List<MediaServerItem>> getMediaServerList(){
        WVPResult<List<MediaServerItem>> result = new WVPResult<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(mediaServerService.getAll());
        return result;
    }

    @ApiOperation("在线流媒体服务列表")
    @GetMapping(value = "/media_server/online/list")
    @ResponseBody
    public WVPResult<List<MediaServerItem>> getOnlineMediaServerList(){
        WVPResult<List<MediaServerItem>> result = new WVPResult<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(mediaServerService.getAllOnline());
        return result;
    }

    @ApiOperation("获取流媒体服务")
    @GetMapping(value = "/media_server/one/{id}")
    @ResponseBody
    public WVPResult<MediaServerItem> getMediaServer(@PathVariable String id){
        WVPResult<MediaServerItem> result = new WVPResult<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(mediaServerService.getOne(id));
        return result;
    }

    @ApiOperation("重启服务")
    @GetMapping(value = "/restart")
    @ResponseBody
    public Object restart(){
        Thread restartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    SipProvider up = (SipProvider) SpringBeanFactory.getBean("udpSipProvider");
                    SipStackImpl stack = (SipStackImpl)up.getSipStack();
                    stack.stop();
                    Iterator listener = stack.getListeningPoints();
                    while (listener.hasNext()) {
                        stack.deleteListeningPoint((ListeningPoint) listener.next());
                    }
                    Iterator providers = stack.getSipProviders();
                    while (providers.hasNext()) {
                        stack.deleteSipProvider((SipProvider) providers.next());
                    }
                    VManageBootstrap.restart();
                } catch (InterruptedException ignored) {
                } catch (ObjectInUseException e) {
                    e.printStackTrace();
                }
            }
        });

        restartThread.setDaemon(false);
        restartThread.start();
        return "success";
    }
}
