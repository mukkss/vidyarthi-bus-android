const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Automatically expires crowd reports older than 15 minutes.
 * Runs every 5 minutes.
 */
exports.expireReports = functions.pubsub.schedule('every 5 minutes').onRun(async (context) => {
    const now = Date.now();
    const fifteenMinutesAgo = now - (15 * 60 * 1000);
    
    const db = admin.database();
    const reportsRef = db.ref('crowd_reports');
    
    const snapshot = await reportsRef.once('value');
    const routes = snapshot.val();
    
    if (!routes) return null;
    
    const updates = {};
    
    for (const routeId in routes) {
        const reports = routes[routeId];
        for (const reportId in reports) {
            if (reports[reportId].timestamp < fifteenMinutesAgo) {
                updates[`/crowd_reports/${routeId}/${reportId}`] = null;
            }
        }
    }
    
    return reportsRef.update(updates);
});
