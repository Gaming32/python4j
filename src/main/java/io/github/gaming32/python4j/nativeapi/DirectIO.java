package io.github.gaming32.python4j.nativeapi;

import java.io.FileDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;

import io.github.gaming32.python4j.util.UnsafeUtil;

public final class DirectIO {
    private static interface CommonC extends Library {
        CommonC INSTANCE = Native.load(Platform.C_LIBRARY_NAME, CommonC.class);

        boolean isatty(int fd);
        String strerror(int errnum);
    }

    private static interface Nt extends Library {
        Nt INSTANCE = Native.load(Platform.C_LIBRARY_NAME, Nt.class);

        int _wopen(WString filename, int oflag, int pmode);
        int _close(int fd);
        int _lseeki64(int fd, long offset, int origin);
        int _read(int fd, byte[] buffer, int buffer_size);
        int _write(int fd, byte[] buffer, int count);
        int _chsize_s(int fd, long size);
        int _wopen(WString filename, int oflag);
        int _open_osfhandle(long osfhandle, int flags);
        long _get_osfhandle(int fd);
    }

    private static interface Posix extends Library {
        Posix INSTANCE = Native.load(Platform.C_LIBRARY_NAME, Posix.class);

        int open(String path, int flags, int mode);
        int close(int fd);
        int lseek(int fd, long offset, int whence);
        long read(int fd, byte[] buf, long nbytes);
        long write(int fd, byte[] buf, long count);
        int pipe(int[] pipefd);
        int pipe2(int[] pipefd, int flags);
        int ftruncate(int fd, long length);
        int truncate(String path, long length);
    }

    // region Open flags
    public static final int O_RDONLY = 0;
    public static final int O_LARGEFILE = 0;
    public static final int O_WRONLY = 1;
    public static final int O_RDWR = 2;
    public static final int O_ACCMODE = 3;
    public static final int O_APPEND = 8;
    public static final int O_RANDOM = 16;
    public static final int O_SEQUENTIAL = 32;
    public static final int O_TEMPORARY = 64;
    public static final int O_NOINHERIT = 128;
    public static final int O_CREAT = 256;
    public static final int O_NOCTTY = 256;
    public static final int O_TRUNC = 512;
    public static final int O_EXCL = 1024;
    public static final int O_NDELAY = 2048;
    public static final int O_NONBLOCK = 2048;
    public static final int O_SHORT_LIVED = 4096;
    public static final int O_DSYNC = 4096;
    public static final int O_ASYNC = 8192;
    public static final int O_TEXT = 16384;
    public static final int O_DIRECT = 16384;
    public static final int O_BINARY = 32768;
    public static final int O_DIRECTORY = 65536;
    public static final int O_NOFOLLOW = 131072;
    public static final int O_NOATIME = 262144;
    public static final int O_CLOEXEC = 524288;
    public static final int O_RSYNC = 1052672;
    public static final int O_SYNC = 1052672;
    public static final int O_PATH = 2097152;
    public static final int O_TMPFILE = 4259840;
    // endregion

    // region Error codes
    public static final int EPERM       =  1;  /* Operation not permitted */
    public static final int ENOENT      =  2;  /* No such file or directory */
    public static final int ESRCH       =  3;  /* No such process */
    public static final int EINTR       =  4;  /* Interrupted system call */
    public static final int EIO         =  5;  /* I/O error */
    public static final int ENXIO       =  6;  /* No such device or address */
    public static final int E2BIG       =  7;  /* Argument list too long */
    public static final int ENOEXEC     =  8;  /* Exec format error */
    public static final int EBADF       =  9;  /* Bad file number */
    public static final int ECHILD      = 10;  /* No child processes */
    public static final int EAGAIN      = 11;  /* Try again */
    public static final int ENOMEM      = 12;  /* Out of memory */
    public static final int EACCES      = 13;  /* Permission denied */
    public static final int EFAULT      = 14;  /* Bad address */
    public static final int ENOTBLK     = 15;  /* Block device required */
    public static final int EBUSY       = 16;  /* Device or resource busy */
    public static final int EEXIST      = 17;  /* File exists */
    public static final int EXDEV       = 18;  /* Cross-device link */
    public static final int ENODEV      = 19;  /* No such device */
    public static final int ENOTDIR     = 20;  /* Not a directory */
    public static final int EISDIR      = 21;  /* Is a directory */
    public static final int EINVAL      = 22;  /* Invalid argument */
    public static final int ENFILE      = 23;  /* File table overflow */
    public static final int EMFILE      = 24;  /* Too many open files */
    public static final int ENOTTY      = 25;  /* Not a typewriter */
    public static final int ETXTBSY     = 26;  /* Text file busy */
    public static final int EFBIG       = 27;  /* File too large */
    public static final int ENOSPC      = 28;  /* No space left on device */
    public static final int ESPIPE      = 29;  /* Illegal seek */
    public static final int EROFS       = 30;  /* Read-only file system */
    public static final int EMLINK      = 31;  /* Too many links */
    public static final int EPIPE       = 32;  /* Broken pipe */
    public static final int EDOM        = 33;  /* Math argument out of domain of func */
    public static final int ERANGE      = 34;  /* Math result not representable */
    public static final int EDEADLK     = 35;  /* Resource deadlock would occur */
    public static final int ENAMETOOLONG = 36; /* File name too long */
    public static final int ENOLCK      = 37;  /* No record locks available */
    public static final int ENOSYS      = 38;  /* Invalid system call number */
    public static final int ENOTEMPTY   = 39;  /* Directory not empty */
    public static final int ELOOP       = 40;  /* Too many symbolic links encountered */
    public static final int EWOULDBLOCK = EAGAIN;  /* Operation would block */
    public static final int ENOMSG      = 42;  /* No message of desired type */
    public static final int EIDRM       = 43;  /* Identifier removed */
    public static final int ECHRNG      = 44;  /* Channel number out of range */
    public static final int EL2NSYNC    = 45;  /* Level 2 not synchronized */
    public static final int EL3HLT      = 46;  /* Level 3 halted */
    public static final int EL3RST      = 47;  /* Level 3 reset */
    public static final int ELNRNG      = 48;  /* Link number out of range */
    public static final int EUNATCH     = 49;  /* Protocol driver not attached */
    public static final int ENOCSI      = 50;  /* No CSI structure available */
    public static final int EL2HLT      = 51;  /* Level 2 halted */
    public static final int EBADE       = 52;  /* Invalid exchange */
    public static final int EBADR       = 53;  /* Invalid request descriptor */
    public static final int EXFULL      = 54;  /* Exchange full */
    public static final int ENOANO      = 55;  /* No anode */
    public static final int EBADRQC     = 56;  /* Invalid request code */
    public static final int EBADSLT     = 57;  /* Invalid slot */
    public static final int EDEADLOCK   = EDEADLK;
    public static final int EBFONT      = 59;  /* Bad font file format */
    public static final int ENOSTR      = 60;  /* Device not a stream */
    public static final int ENODATA     = 61;  /* No data available */
    public static final int ETIME       = 62;  /* Timer expired */
    public static final int ENOSR       = 63;  /* Out of streams resources */
    public static final int ENONET      = 64;  /* Machine is not on the network */
    public static final int ENOPKG      = 65;  /* Package not installed */
    public static final int EREMOTE     = 66;  /* Object is remote */
    public static final int ENOLINK     = 67;  /* Link has been severed */
    public static final int EADV        = 68;  /* Advertise error */
    public static final int ESRMNT      = 69;  /* Srmount error */
    public static final int ECOMM       = 70;  /* Communication error on send */
    public static final int EPROTO      = 71;  /* Protocol error */
    public static final int EMULTIHOP   = 72;  /* Multihop attempted */
    public static final int EDOTDOT     = 73;  /* RFS specific error */
    public static final int EBADMSG     = 74;  /* Not a data message */
    public static final int EOVERFLOW   = 75;  /* Value too large for defined data type */
    public static final int ENOTUNIQ    = 76;  /* Name not unique on network */
    public static final int EBADFD      = 77;  /* File descriptor in bad state */
    public static final int EREMCHG     = 78;  /* Remote address changed */
    public static final int ELIBACC     = 79;  /* Can not access a needed shared library */
    public static final int ELIBBAD     = 80;  /* Accessing a corrupted shared library */
    public static final int ELIBSCN     = 81;  /* .lib section in a.out corrupted */
    public static final int ELIBMAX     = 82;  /* Attempting to link in too many shared libraries */
    public static final int ELIBEXEC    = 83;  /* Cannot exec a shared library directly */
    public static final int EILSEQ      = 84;  /* Illegal byte sequence */
    public static final int ERESTART    = 85;  /* Interrupted system call should be restarted */
    public static final int ESTRPIPE    = 86;  /* Streams pipe error */
    public static final int EUSERS      = 87;  /* Too many users */
    public static final int ENOTSOCK    = 88;  /* Socket operation on non-socket */
    public static final int EDESTADDRREQ = 89; /* Destination address required */
    public static final int EMSGSIZE    = 90;  /* Message too long */
    public static final int EPROTOTYPE  = 91;  /* Protocol wrong type for socket */
    public static final int ENOPROTOOPT = 92;  /* Protocol not available */
    public static final int EPROTONOSUPPORT = 93;  /* Protocol not supported */
    public static final int ESOCKTNOSUPPORT = 94;  /* Socket type not supported */
    public static final int EOPNOTSUPP  = 95;  /* Operation not supported on transport endpoint */
    public static final int EPFNOSUPPORT = 96; /* Protocol family not supported */
    public static final int EAFNOSUPPORT = 97; /* Address family not supported by protocol */
    public static final int EADDRINUSE  = 98;  /* Address already in use */
    public static final int EADDRNOTAVAIL = 99;  /* Cannot assign requested address */
    public static final int ENETDOWN    = 100; /* Network is down */
    public static final int ENETUNREACH = 101; /* Network is unreachable */
    public static final int ENETRESET   = 102; /* Network dropped connection because of reset */
    public static final int ECONNABORTED = 103; /* Software caused connection abort */
    public static final int ECONNRESET  = 104; /* Connection reset by peer */
    public static final int ENOBUFS     = 105; /* No buffer space available */
    public static final int EISCONN     = 106; /* Transport endpoint is already connected */
    public static final int ENOTCONN    = 107; /* Transport endpoint is not connected */
    public static final int ESHUTDOWN   = 108; /* Cannot send after transport endpoint shutdown */
    public static final int ETOOMANYREFS = 109; /* Too many references: cannot splice */
    public static final int ETIMEDOUT   = 110; /* Connection timed out */
    public static final int ECONNREFUSED = 111; /* Connection refused */
    public static final int EHOSTDOWN   = 112; /* Host is down */
    public static final int EHOSTUNREACH = 113; /* No route to host */
    public static final int EALREADY    = 114; /* Operation already in progress */
    public static final int EINPROGRESS = 115; /* Operation now in progress */
    public static final int ESTALE      = 116; /* Stale file handle */
    public static final int EUCLEAN     = 117; /* Structure needs cleaning */
    public static final int ENOTNAM     = 118; /* Not a XENIX named type file */
    public static final int ENAVAIL     = 119; /* No XENIX semaphores available */
    public static final int EISNAM      = 120; /* Is a named type file */
    public static final int EREMOTEIO   = 121; /* Remote I/O error */
    public static final int EDQUOT      = 122; /* Quota exceeded */
    public static final int ENOMEDIUM   = 123; /* No medium found */
    public static final int EMEDIUMTYPE = 124; /* Wrong medium type */
    public static final int ECANCELED   = 125; /* Operation Canceled */
    public static final int ENOKEY      = 126; /* Required key not available */
    public static final int EKEYEXPIRED = 127; /* Key has expired */
    public static final int EKEYREVOKED = 128; /* Key has been revoked */
    public static final int EKEYREJECTED = 129; /* Key was rejected by service */
    public static final int EOWNERDEAD  = 130; /* Owner died */
    public static final int ENOTRECOVERABLE = 131; /* State not recoverable */
    public static final int ERFKILL     = 132; /* Operation not possible due to RF-kill */
    public static final int EHWPOISON   = 133; /* Memory page has hardware error */
    // endregion

    // region Seek whence codes
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    public static final int SEEK_DATA = 3;
    public static final int SEEK_HOLE = 4;
    // endregion

    private static final MethodHandle FD_NEW_I;
    private static final VarHandle FD_FD;
    private static final VarHandle FD_HANDLE;

    static {
        final MethodHandles.Lookup lookup = UnsafeUtil.getTrustedLookup();
        try {
            FD_NEW_I = lookup.findConstructor(FileDescriptor.class, MethodType.methodType(void.class, int.class));
            FD_FD = lookup.findVarHandle(FileDescriptor.class, "fd", int.class);
            FD_HANDLE = lookup.findVarHandle(FileDescriptor.class, "handle", long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private DirectIO() {
    }

    public static int open(String path, int flags) {
        return open(path, flags, 511);
    }

    public static int open(String path, int flags, int mode) {
        if (Platform.isWindows()) {
            flags |= O_NOINHERIT;
        } else {
            flags |= O_CLOEXEC;
        }

        int fd;
        do {
            if (Platform.isWindows()) {
                fd = Nt.INSTANCE._wopen(new WString(path), flags, mode);
            } else {
                fd = Posix.INSTANCE.open(path, flags, 0);
            }
        } while (fd < 0 && Native.getLastError() == EINTR);

        if (fd < 0) {
            posixError(); // TODO: pass filenameObject
        }

        return fd;
    }

    public static void close(int fd) {
        int res;
        if (Platform.isWindows()) {
            res = Nt.INSTANCE._close(fd);
        } else {
            res = Posix.INSTANCE.close(fd);
        }
        if (res < 0) {
            posixError();
        }
    }

    public static int lseek(int fd, long position, int how) {
        int result;
        if (Platform.isWindows()) {
            result = Nt.INSTANCE._lseeki64(fd, position, how);
        } else {
            result = Posix.INSTANCE.lseek(fd, position, how);
        }
        if (result < 0) {
            posixError();
        }
        return result;
    }

    public static byte[] read(int fd, int length) {
        if (length < 0) {
            Native.setLastError(EINVAL);
            posixError();
        }

        final byte[] buffer = new byte[length];

        int n, err;
        do {
            Native.setLastError(0);
            if (Platform.isWindows()) {
                n = Nt.INSTANCE._read(fd, buffer, length);
            } else {
                n = (int)Posix.INSTANCE.read(fd, buffer, length);
            }
            err = Native.getLastError();
        } while (n < 0 && err == EINTR);

        if (n < 0) {
            try {
                posixError();
            } catch (RuntimeException e) {
                Native.setLastError(err);
                throw e;
            }
        }

        return n != length ? Arrays.copyOf(buffer, n) : buffer;
    }

    public static int write(int fd, byte[] data) {
        return write(fd, data, data.length);
    }

    public static int write(int fd, byte[] data, int len) {
        if (len > 33767 && Platform.isWindows()) {
            if (CommonC.INSTANCE.isatty(fd)) {
                len = 32767;
            }
        }

        int n, err;
        do {
            Native.setLastError(0);
            if (Platform.isWindows()) {
                n = Nt.INSTANCE._write(fd, data, len);
            } else {
                n = (int)Posix.INSTANCE.write(fd, data, len);
            }
            err = Native.getLastError();
        } while (n < 0 && err == EINTR);

        if (n < 0) {
            try {
                posixError();
            } catch (RuntimeException e) {
                Native.setLastError(err);
                throw e;
            }
        }

        return n;
    }

    public static boolean isatty(int fd) {
        return CommonC.INSTANCE.isatty(fd);
    }

    public static int[] pipe() {
        int[] fds = new int[2];
        if (Platform.isWindows()) {
            HANDLEByReference read = new HANDLEByReference(), write = new HANDLEByReference();
            SECURITY_ATTRIBUTES attr = new SECURITY_ATTRIBUTES();
            boolean ok = Kernel32.INSTANCE.CreatePipe(read, write, attr, 0);
            if (ok) {
                fds[0] = Nt.INSTANCE._open_osfhandle(Pointer.nativeValue(read.getValue().getPointer()), O_RDONLY);
                fds[1] = Nt.INSTANCE._open_osfhandle(Pointer.nativeValue(write.getValue().getPointer()), O_WRONLY);
                if (fds[0] == -1 || fds[1] == -1) {
                    Kernel32.INSTANCE.CloseHandle(read.getValue());
                    Kernel32.INSTANCE.CloseHandle(write.getValue());
                    ok = false;
                }
            }

            if (!ok) {
                windowsError(0);
            }
        } else {
            int res = Posix.INSTANCE.pipe2(fds, O_CLOEXEC);
            if (res != 0 && Native.getLastError() == ENOSYS) {
                res = Posix.INSTANCE.pipe(fds);
            }
            if (res != 0) {
                posixError();
            }
        }
        return fds;
    }

    public static void ftruncate(int fd, long length) {
        int result;

        do {
            if (Platform.isWindows()) {
                result = Nt.INSTANCE._chsize_s(fd, length);
            } else {
                result = Posix.INSTANCE.ftruncate(fd, length);
            }
        } while (result != 0 && Native.getLastError() == EINTR);
        if (result != 0) {
            posixError();
        }
    }

    public static void truncate(int fd, long length) {
        ftruncate(fd, length);
    }

    public static void truncate(String path, long length) {
        int result;
        if (Platform.isWindows()) {
            final int fd = Nt.INSTANCE._wopen(new WString(path), O_WRONLY | O_BINARY | O_NOINHERIT);
            if (fd < 0) {
                result = -1;
            } else {
                result = Nt.INSTANCE._chsize_s(fd, length);
                Nt.INSTANCE._close(fd);
                if (result < 0) {
                    Native.setLastError(result);
                }
            }
        } else {
            result = Posix.INSTANCE.truncate(path, length);
        }
        if (result < 0) {
            posixError(); // TODO: pass filenameObject
        }
    }

    public static FileDescriptor toJavaFileDescriptor(int fd) {
        try {
            return (FileDescriptor)FD_NEW_I.invoke(fd);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            if (e instanceof Error) {
                throw (Error)e;
            }
            throw new RuntimeException(e);
        }
    }

    public static int getFd(FileDescriptor fd) {
        return (int)FD_FD.get(fd);
    }

    public static void setFd(FileDescriptor fd, int fd0) {
        FD_FD.set(fd, fd0);
    }

    public static long getHandle(FileDescriptor fd) {
        return (long)FD_HANDLE.get(fd);
    }

    public static void setHandle(FileDescriptor fd, long handle) {
        FD_HANDLE.set(fd, handle);
    }

    public static int openHandle(long handle, int flags) {
        if (!Platform.isWindows()) {
            throw new IllegalStateException("Handles are Windows-specific.");
        }
        return Nt.INSTANCE._open_osfhandle(handle, flags);
    }

    public static long getHandle(int fd) {
        if (!Platform.isWindows()) {
            throw new IllegalStateException("Handles are Windows-specific.");
        }
        return Nt.INSTANCE._get_osfhandle(fd);
    }

    public static int getOrOpenFd(FileDescriptor fd, int flags) {
        int fd0 = getFd(fd);
        if (fd0 < 0 || !Platform.isWindows()) {
            return fd0;
        }
        return Nt.INSTANCE._open_osfhandle(getHandle(fd), flags);
    }

    public static long getOrGetHandle(FileDescriptor fd) {
        long handle = getHandle(fd);
        if (handle < 0L || !Platform.isWindows()) {
            return handle;
        }
        return Nt.INSTANCE._get_osfhandle(getFd(fd));
    }

    private static void posixError() {
        final int errno = Native.getLastError();
        String message;
        if (errno == 0) {
            message = "Error";
        } else if (errno <= 42 || !Platform.isWindows()) {
            message = CommonC.INSTANCE.strerror(errno);
        } else {
            try {
                message = Kernel32Util.formatMessage(errno);
            } catch (LastErrorException e) {
                message = "Windows error 0x" + Integer.toHexString(errno);
            }
        }
        throw new RuntimeException("[Errno " + errno + "] " + message);
    }

    private static void windowsError(int err) {
        if (err == 0) {
            err = Native.getLastError();
        }

        String message;
        try {
            message = Kernel32Util.formatMessage(err);
        } catch (LastErrorException e) {
            message = "Windows error 0x" + Integer.toHexString(err);
        }

        throw new RuntimeException(message);
    }
}
