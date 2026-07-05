import api from './api';

export class DocumentService {
  async getByUri(uri: string): Promise<Blob> {
    const response = await api.get(uri, {
      responseType: 'blob',
    });
    return response.data;
  }

  async upload(file: File): Promise<string> {
    if (!(file instanceof File)) {
      throw new Error('Invalid file object');
    }

    if (file.size === 0) {
      throw new Error('File is empty');
    }

    const formData = new FormData();

    formData.append('document', file, file.name);
    formData.append('isPublic', 'true');

    const response = await api.post('/documents', formData);

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on document creation response');
    }

    return location;
  }

  async uploadReceipt(file: File): Promise<string> {
    if (!(file instanceof File)) {
      throw new Error('Invalid file object');
    }

    if (file.size === 0) {
      throw new Error('File is empty');
    }

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('Invalid file type. Only PDF, JPEG, JPG, and PNG are allowed.');
    }

    const formData = new FormData();
    formData.append('document', file, file.name);
    formData.append('isPublic', 'false');

    const response = await api.post('/documents', formData);

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on document creation response');
    }

    return location;
  }
}

export const documentService = new DocumentService();
