package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validations.AllowedFiletypes;
import ar.edu.itba.paw.webapp.form.validations.AllowedSize;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.constraints.NotNull;

public class DocumentForm {

    @NotNull
    @AllowedFiletypes(message = "{AllowedFiletypes.document}")
    @FormDataParam("document")
    private FormDataBodyPart formDataBodyPart;

    @NotNull
    @AllowedSize(message = "{AllowedSize.document}")
    @FormDataParam("document")
    private byte[] bytes;

    @NotNull
    @FormDataParam("isPublic")
    private Boolean isPublic;

    public FormDataBodyPart getFormDataBodyPart() {
        return formDataBodyPart;
    }

    public void setFormDataBodyPart(FormDataBodyPart formDataBodyPart) {
        this.formDataBodyPart = formDataBodyPart;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

}
