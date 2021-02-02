package com.yhmsi.job.record.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * <Description>
 *
 * @author ZhengWei
 * @version 1.0
 * @taskId:
 * @createDate 2021/02/01 13:27
 * @see com.yhmsi.job.record.utils
 */



public class SvnUtils {
    private static Logger log = LoggerFactory.getLogger(SvnUtils.class);
    private String userName = "zhengwei"; //svn账号
    private String password = "VN44W6KbCbR6tygv"; //svn密码
    private String urlString = "https://47.112.245.4/svn/rcm_vis_glwjw"; //svnurl

    private String tempDir = System.getProperty("java.io.tmpdir"); //临时文件
    private DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);

    private SVNRepository repos;
    private ISVNAuthenticationManager authManager;

    public SvnUtils() {
        try {
            init();
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }
    public void init() throws SVNException{
        log.info("开始加载");
        authManager = SVNWCUtil.createDefaultAuthenticationManager(new File(tempDir+"/auth"), userName, password.toCharArray());
        options.setDiffCommand("-x -w");
        repos = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(urlString));
        repos.setAuthenticationManager(authManager);
        log.info("init completed");
    }

    /**获取一段时间内，所有的commit记录
     * @param st	开始时间
     * @param et	结束时间
     * @return
     * @throws SVNException
     */
    public SVNLogEntry[] getLogByTime(Date st, Date et) throws SVNException{
        long startRevision = repos.getDatedRevision(st);
        long endRevision = repos.getDatedRevision(et);
        @SuppressWarnings("unchecked")
        Collection<SVNLogEntry> logEntries = repos.log(new String[]{""}, null,
                startRevision, endRevision, true, true);
        SVNLogEntry[] svnLogEntries = logEntries.toArray(new SVNLogEntry[0]);
        SVNLogEntry[] svnLogEntries1 = null;
        if(svnLogEntries.length==0){
            svnLogEntries1 = Arrays.copyOf(svnLogEntries, svnLogEntries.length);
        }else{
            svnLogEntries1 = Arrays.copyOf(svnLogEntries, svnLogEntries.length-1);
        }
        return svnLogEntries1;
    }

    public static void main(String[] args) throws SVNException, ParseException {
        SvnUtils svnUtils = new SvnUtils();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2021-01-01");
        Date endDate = new Date();
        SVNLogEntry[] logByTime = svnUtils.getLogByTime(startDate, endDate);
        for (SVNLogEntry svnLogEntry : logByTime) {
            System.out.println(svnLogEntry.getMessage());
        }
    }

}
