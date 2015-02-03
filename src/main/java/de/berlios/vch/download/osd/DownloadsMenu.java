package de.berlios.vch.download.osd;

import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.osgi.service.log.LogService;

import de.berlios.vch.download.Download;
import de.berlios.vch.download.DownloadManager;
import de.berlios.vch.download.jaxb.DownloadDTO;
import de.berlios.vch.osdserver.io.StringUtils;
import de.berlios.vch.osdserver.osd.OsdItem;
import de.berlios.vch.osdserver.osd.menu.Menu;
import de.berlios.vch.playlist.PlaylistService;

public class DownloadsMenu extends Menu {

    public DownloadsMenu(DownloadManager dm, LogService logger, ResourceBundle rb, Preferences prefs,
            PlaylistService pls) {
        super("downloads", rb.getString("I18N_DOWNLOADS"));

        // create actions
        DeleteAction deleteAction = new DeleteAction(rb, dm);
        CancelAction cancelAction = new CancelAction(rb, dm);
        UpdateColorButtonsAction updateColorButtons = new UpdateColorButtonsAction(logger);
        PlayFinishedAction playFinished = new PlayFinishedAction(rb);
        PlayActiveAction playActive = new PlayActiveAction(rb);
        StartStopAction startStop = new StartStopAction(rb, dm);
        ChangeOrderAction sortAction = new ChangeOrderAction(dm, logger, rb, prefs, pls);

        // add the active downloads to the menu
        List<Download> activeDownloads = dm.getActiveDownloads();
        if (!activeDownloads.isEmpty()) {
            // headline for active downloads
            OsdItem headActive = new OsdItem("headActive", rb.getString("I18N_DOWNLOADS"));
            headActive.setSelectable(false);
            addOsdItem(headActive);
        }
        for (int i = 0; i < activeDownloads.size(); i++) {
            Download download = activeDownloads.get(i);
            String downloadId = "download" + i;
            String line = formatActiveDownload(download);
            OsdItem osditem = new OsdItem(downloadId, line);
            osditem.setUserData(download);
            osditem.registerAction(updateColorButtons);
            osditem.registerAction(playActive);
            osditem.registerAction(cancelAction);
            if (download.isPauseSupported()) {
                startStop.setName(download.isRunning() ? rb.getString("I18N_DL_STOP") : rb.getString("I18N_DL_START"));
                osditem.registerAction(startStop);
            }
            addOsdItem(osditem);
        }

        // add the finished downloads to the menu
        List<DownloadDTO> finishedDownloads = dm.getFinishedDownloads();
        if (!finishedDownloads.isEmpty()) {
            // headline for active downloads
            OsdItem headFinished = new OsdItem("headFinished", rb.getString("I18N_DL_FINISHED_DOWNLOADS"));
            headFinished.setSelectable(false);
            addOsdItem(headFinished);
        }
        for (int i = 0; i < finishedDownloads.size(); i++) {
            DownloadDTO download = finishedDownloads.get(i);
            String downloadId = "finished_download" + i;
            String title = download.getTitle();
            OsdItem osditem = new OsdItem(downloadId, StringUtils.escape(title));
            osditem.setUserData(download);
            osditem.registerAction(updateColorButtons);
            osditem.registerAction(deleteAction);
            osditem.registerAction(playFinished);
            osditem.registerAction(sortAction);
            addOsdItem(osditem);
        }
    }

    public static String formatActiveDownload(Download d) {
        return d.getProgress() + "% " + d.getStatus() + " " + d.getVideoPage().getTitle();
    }
}
