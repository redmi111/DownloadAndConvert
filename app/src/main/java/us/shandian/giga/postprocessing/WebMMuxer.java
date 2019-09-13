package us.shandian.giga.postprocessing;

import java.io.IOException;

import us.shandian.giga.streams.WebMReader.TrackKind;
import us.shandian.giga.streams.WebMReader.WebMTrack;
import us.shandian.giga.streams.WebMWriter;
import us.shandian.giga.streams.io.SharpStream;

/**
 * @author kapodamy
 */
class WebMMuxer extends Postprocessing {

    WebMMuxer() {
        super(true, true, ALGORITHM_WEBM_MUXER);
    }

    @Override
    int process(SharpStream out, SharpStream... sources) throws IOException {
        WebMWriter muxer = new WebMWriter(sources);
        muxer.parseSources();

        // youtube uses a webm with a fake video track that acts as a "cover image"
        int[] indexes = new int[sources.length];

        for (int i = 0; i < sources.length; i++) {
            WebMTrack[] tracks = muxer.getTracksFromSource(i);
            for (int j = 0; j < tracks.length; j++) {
                if (tracks[j].kind == TrackKind.Audio) {
                    indexes[i] = j;
                    i = sources.length;
                    break;
                }
            }
        }

        muxer.selectTracks(indexes);
        muxer.build(out);

        return OK_RESULT;
    }

}
