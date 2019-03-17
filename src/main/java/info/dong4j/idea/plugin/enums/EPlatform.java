package info.dong4j.idea.plugin.enums;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-17 23:17
 * @email sjdong3@iflytek.com
 */
public enum EPlatform {
    Any("any"),  
    Linux("Linux"),  
    Mac_OS("Mac OS"),  
    Mac_OS_X("Mac OS X"),  
    Windows("Windows"),  
    OS2("OS/2"),  
    Solaris("Solaris"),  
    SunOS("SunOS"),  
    MPEiX("MPE/iX"),  
    HP_UX("HP-UX"),  
    AIX("AIX"),  
    OS390("OS/390"),  
    FreeBSD("FreeBSD"),  
    Irix("Irix"),  
    Digital_Unix("Digital Unix"),  
    NetWare_411("NetWare"),  
    OSF1("OSF1"),  
    OpenVMS("OpenVMS"),  
    Others("Others");  

    EPlatform(String desc){
        this.description = desc;  
    }  

    @Override
    public String toString(){
        return description;  
    }  

    private String description;  
}