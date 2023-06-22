
@Ignore
Feature: Common functionality

  Scenario:
    * def reset =
"""
function(args) {
     var KarateClient = Java.type('com.argosnotary.argos.service.itest.KarateClient');
     return KarateClient.resetNotAllRepositories();
}
"""
    * def getAuditLogs =
"""
function() {
     var KarateClient = Java.type('com.argosnotary.argos.service.itest.KarateClient');
     return KarateClient.getAuditLogs();
}
"""    
    * def signLayout =
"""
function(args) {
     var KarateClient = Java.type('com.argosnotary.argos.service.itest.KarateClient');
     return KarateClient.signLayout(args.passphrase, karate.toJava(args.keyPair), karate.toJava   (args.layoutMetaBlock));
}
"""
    * def signLink =
"""
function(args) {
     var KarateClient = Java.type('com.argosnotary.argos.service.itest.KarateClient');
     return KarateClient.signLink(args.passphrase, karate.toJava(args.keyPair), karate.toJava   (args.linkMetaBlock));
}
"""
    * def paLogin =
"""
function(userName) {
     var KarateClient = Java.type('com.argosnotary.argos.service.itest.KarateClient');
     return KarateClient.paLogin(userName);
}
"""