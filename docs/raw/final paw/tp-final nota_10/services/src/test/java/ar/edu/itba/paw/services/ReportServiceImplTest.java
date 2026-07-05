package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ReportDao;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceImplTest {

    private static final int USER_ID = 10;
    private static final String DESCRIPTION = "Test report description";
    private static final int PAGE = 0;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private ReportDao reportDao;

    @Mock
    private UserService userService;

    // tests para create
    @Test
    public void testCreateSuccessfully() {
        Company company = mock(Company.class);
        User user = mock(User.class);
        Report report = mock(Report.class);

        when(userService.findById(USER_ID)).thenReturn(Optional.of(user));
        when(reportDao.create(eq(DESCRIPTION), any(Date.class), eq(true), eq(company), eq(user))).thenReturn(report);

        Report result = reportService.create(DESCRIPTION, company, USER_ID);

        assertNotNull(result);
        assertEquals(report, result);
        verify(userService).findById(eq(USER_ID));
        verify(reportDao).create(eq(DESCRIPTION), any(Date.class), eq(true), eq(company), eq(user));
    }

    @Test
    public void testCreateVerifiesDateIsNotNull() {
        Company company = mock(Company.class);
        User user = mock(User.class);
        Report report = mock(Report.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        when(userService.findById(USER_ID)).thenReturn(Optional.of(user));
        when(reportDao.create(eq(DESCRIPTION), dateCaptor.capture(), eq(true), eq(company), eq(user))).thenReturn(report);

        reportService.create(DESCRIPTION, company, USER_ID);

        assertNotNull(dateCaptor.getValue());
    }

    @Test(expected = UserNotFoundException.class)
    public void testCreateWithNonExistentUserThrows() {
        Company company = mock(Company.class);

        when(userService.findById(USER_ID)).thenReturn(Optional.empty());

        reportService.create(DESCRIPTION, company, USER_ID);
    }

    // tests para filter
    @Test
    public void testFilterWithMultipleReports() {
        Report report1 = mock(Report.class);
        Report report2 = mock(Report.class);
        Paginator<Report> paginator = new Paginator<>(Arrays.asList(report1, report2), PAGE, 12, 2);

        when(reportDao.filter(USER_ID, PAGE)).thenReturn(paginator);

        Paginator<Report> result = reportService.filter(USER_ID, PAGE);

        assertNotNull(result);
        assertEquals(2, result.getList().size());
        assertEquals(report1, result.getList().get(0));
        assertEquals(report2, result.getList().get(1));
        verify(reportDao).filter(eq(USER_ID), eq(PAGE));
    }

    @Test
    public void testFilterWithEmptyList() {
        Paginator<Report> paginator = new Paginator<>();

        when(reportDao.filter(USER_ID, PAGE)).thenReturn(paginator);

        Paginator<Report> result = reportService.filter(USER_ID, PAGE);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(reportDao).filter(eq(USER_ID), eq(PAGE));
    }

    @Test
    public void testFilterWithNegativePageClampsToZero() {
        Paginator<Report> paginator = new Paginator<>();

        when(reportDao.filter(USER_ID, 0)).thenReturn(paginator);

        Paginator<Report> result = reportService.filter(USER_ID, -5);

        assertNotNull(result);
        verify(reportDao).filter(eq(USER_ID), eq(0));
    }

    @Test
    public void testFilterWithSingleReport() {
        Report report = mock(Report.class);
        Paginator<Report> paginator = new Paginator<>(Collections.singletonList(report), PAGE, 12, 1);

        when(reportDao.filter(USER_ID, PAGE)).thenReturn(paginator);

        Paginator<Report> result = reportService.filter(USER_ID, PAGE);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals(report, result.getList().getFirst());
        verify(reportDao).filter(eq(USER_ID), eq(PAGE));
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        int reportId = 1;
        Report report = mock(Report.class);

        when(reportDao.findById(reportId)).thenReturn(Optional.of(report));

        Optional<Report> result = reportService.findById(reportId);

        assertTrue(result.isPresent());
        assertEquals(report, result.get());
        verify(reportDao).findById(eq(reportId));
    }

    @Test
    public void testFindByIdNotExists() {
        int reportId = 1;

        when(reportDao.findById(reportId)).thenReturn(Optional.empty());

        Optional<Report> result = reportService.findById(reportId);

        assertFalse(result.isPresent());
        verify(reportDao).findById(eq(reportId));
    }

}
