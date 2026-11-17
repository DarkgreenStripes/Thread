package Helpers;

/**
* Enum global para gestionar los cambios del semáforo entre producción y consumición
*/
public enum CriticalSection {
    IDLE,
    PRODUCING,
    CONSUMING
}
