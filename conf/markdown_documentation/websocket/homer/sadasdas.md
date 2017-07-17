# Tyrion -> homer server protocol

## homer-server::getVerificationToken

**payload**: {}

**response** {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hashToken?: (token)<br>
}

## homer-server::verificationTokenApprove

**payload**: {}

**response** {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## homer-server::serverValidation

TODO ... that means server verification failed?


## homer-server::getServerConfiguration

Sends whole config to tyrion

**payload**: {}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;[key]?: value<br>
}

## homer-server::createInstance

Tyrion requests to create new instance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (requested instance id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## homer-server::setProgram

Tyrion requests to change (or sets) instance blocko program

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (requested instance id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;programId: (requested blocko program id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## homer-server::destroyInstance

Tyrion requests to destroy instance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (requested instance id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## homer-server::numberOfInstances

Tyrion requests to destroy instance

**payload**: {}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;value?: (number of instances)<br>
}

## homer-server::instanceExist

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceIds: [(id1), (id2), ...]<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;instances: {[id]: <span style="color:blue">boolean</span>}<br>
}

## homer-server::listInstances

**payload**: {}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;instances: {[id]: <span style="color:blue">boolean</span>}<br>
}

## instance::pingInstance

**payload**: {
&nbsp;&nbsp;&nbsp;&nbsp;instanceIds: [(id1), (id2), ...]
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;instances: {[id]: <span style="color:blue">boolean</span>}<br>
}

## instance::addHardwareToInstance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (instance id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareId: (hardware id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## instance::removeHardwareFromInstance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (instance id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareId: (hardware id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## instance::addTerminalToInstance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (instance id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;terminalId: (terminal id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## instance::removeTerminalFromInstance

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;instanceId: (instance id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;terminalId: (terminal id)<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## hardware::onlineStatus

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardware: {[id]: <span style="color:blue">boolean</span>}
}

## hardware::updateBinnary

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
&nbsp;&nbsp;&nbsp;&nbsp;binnary: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;component: '<span style="color:red">firmware</span>' | '<span style="color:red">bootloader</span>' | '<span style="color:red">backup</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id: (binary id)<br>
&nbsp;&nbsp;&nbsp;&nbsp;}<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## hardware::settings

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
&nbsp;&nbsp;&nbsp;&nbsp;settings: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias?: <span style="color:blue">string</span><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;autobackup?: <span style="color:blue">boolean</span><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;normal_mqtt_connection?: <span style="color:blue">string</span><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;backup_mqtt_connection?: <span style="color:blue">string</span><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;console?: <span style="color:blue">boolean</span><br>
&nbsp;&nbsp;&nbsp;&nbsp;}<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
}

## hardware::info

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
&nbsp;&nbsp;&nbsp;&nbsp;info: [<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[target]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[alias]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[autobackup]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[normal_mqtt_connection]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[backup_mqtt_connection]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[console]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[memsize]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[state]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[mac_eth]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[ip]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[cpuload]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[datetime]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[uptime]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[binaries]<br>
&nbsp;&nbsp;&nbsp;&nbsp;]<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardware: {[id]: {[info]: (info object) }}<br>
}

## hardware::ping

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardware: {[id]: <span style="color:blue">boolean</span>}<br>
}

## hardware::restart

**payload**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardwareIds: [(id1), (id2), ...]<br>
}

**response**: {<br>
&nbsp;&nbsp;&nbsp;&nbsp;status: '<span style="color:red">success</span>' | '<span style="color:red">error</span>'<br>
&nbsp;&nbsp;&nbsp;&nbsp;errorCode?: (error code)<br>
&nbsp;&nbsp;&nbsp;&nbsp;hardware: {[id]: <span style="color:blue">boolean</span>}<br>
}