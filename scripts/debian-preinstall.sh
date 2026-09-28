#!/bin/sh

# Older setups may have /var/lib/linstor set immutable (chattr +i), which was
# previously recommended in the LINSTOR User's Guide. dpkg cannot re-apply the
# packaged directory's metadata against an immutable directory, so the upgrade
# fails. Clear the flag here (runs before the new files are unpacked). We
# intentionally do not restore it: the flag is no longer recommended. Note: the
# directory path itself contains an 'i', so isolate the attribute field before
# grepping.
if [ -d /var/lib/linstor ] && lsattr -d /var/lib/linstor 2>/dev/null | awk '{print $1}' | grep -q i; then
    chattr -i /var/lib/linstor || :
fi

# DEBHELPER will be replaced by debian build system, adding systemd helper scripts and so on...
#DEBHELPER#

exit 0
