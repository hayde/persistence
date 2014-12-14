/*
 * (C) 2014 hayde.eu
 */
package eu.hayde.box.persistence;

/**
 * you should include this class into your main calling object. Then you could
 * be sure, that the persistence driver does have the correct version number you
 * require.
 *
 * If the new version of hayde persistence driver will be compatible with the
 * previous versions, this classes will stay where they are.
 *
 * If not, the versions, no more compatible, will vanish from here and your
 * software will fire a compilation error. so you wouldn't hunt down
 * compatibility problems.
 *
 * @author can.senturk@hayde.eu
 */
public class Version_1_0 {

    public String description() {
        return "2014-12-14	first public release";
    }
}
