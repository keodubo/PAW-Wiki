package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentFileTypeException;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.webapp.form.DocumentForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;

@Path("/api/documents")
@Component
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    @Context
    private UriInfo uriInfo;

    private final DocumentService documentService;

    @Autowired
    public DocumentController(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @GET
    @Path("/{document_id:\\d+}")
    @Produces({"image/png", "image/jpg", "image/jpeg", "application/pdf"})
    public Response getDocument(@PathParam("document_id") final int documentId, @Context ContainerRequestContext requestContext) {
        final Document document = documentService.findById(documentId).orElseThrow(DocumentNotFoundException::new);
        requestContext.setProperty("isPublicDocument", document.isPublic());
        final Response.ResponseBuilder builder = Response.ok(document.getBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", document.getName()))
                .header(HttpHeaders.CONTENT_TYPE, document.getFileType());
        return builder.build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PreAuthorize("(#form.isPublic and (hasRole('COMPANY_PENDING') or hasRole('COMPANY_UNVERIFIED') or hasRole('COMPANY_VERIFIED'))) or (!#form.isPublic and hasRole('CLIENT'))")
    public Response uploadDocument(@Valid @BeanParam final DocumentForm form) {
        final String fileName = form.getFormDataBodyPart().getContentDisposition().getFileName();
        final String fileType = form.getFormDataBodyPart().getMediaType().toString();
        final boolean isPublic = form.getIsPublic();

        if (isPublic) {
            if (!isImageType(fileType))
                throw new InvalidDocumentFileTypeException();
        } else {
            if (!isImageType(fileType) && !isPdfType(fileType))
                throw new InvalidDocumentFileTypeException();
        }

        final Document document = documentService.create(fileName, fileType, form.getBytes(), isPublic);
        LOGGER.info("Document uploaded: {} (public: {})", document.getId(), isPublic);
        return Response.created(uriInfo.getBaseUriBuilder().path("api/documents").path(String.valueOf(document.getId())).build()).build();
    }

    private boolean isImageType(final String fileType) {
        return fileType != null && (
                fileType.startsWith("image/png") ||
                        fileType.startsWith("image/jpg") ||
                        fileType.startsWith("image/jpeg")
        );
    }

    private boolean isPdfType(final String fileType) {
        return fileType != null && fileType.startsWith("application/pdf");
    }

}
